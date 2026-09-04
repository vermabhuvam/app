package com.gullyrewind.radio.player

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Plays audio from a YouTube video by loading YouTube's own embed page
 * directly in a hidden WebView (https://www.youtube.com/embed/<id>), rather
 * than wrapping the IFrame Player API in custom HTML with a spoofed origin.
 * This is the more standard, reliable technique — nothing is downloaded or
 * re-hosted, matching YouTube's Terms of Service.
 *
 * Control (play/pause) is done by posting messages to the embed page's own
 * `window`, which YouTube's player script listens on — the same mechanism
 * the official IFrame Player API uses under the hood.
 */
class YouTubeAudioPlayer(
    private val webView: WebView,
    private val listener: Listener
) {
    interface Listener {
        fun onPlaying()
        fun onPaused()
        fun onSongEnded()
        fun onPlayerReady()
        fun onError(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(firstVideoId: String) {
        WebView.setWebContentsDebuggingEnabled(true) // inspect via chrome://inspect if needed
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("YTPlayerConsole", "${message.message()} (${message.sourceId()}:${message.lineNumber()})")
                return true
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                injectBridgeScript()
            }
        }
        loadVideo(firstVideoId)
    }

    fun play(videoId: String) {
        loadVideo(videoId)
    }

    fun resume() {
        postCommand("playVideo")
    }

    fun pause() {
        postCommand("pauseVideo")
    }

    private fun loadVideo(videoId: String) {
        val url = "https://www.youtube.com/embed/$videoId" +
            "?autoplay=1&mute=1&playsinline=1&enablejsapi=1&controls=0&rel=0&modestbranding=1"
        mainHandler.post { webView.loadUrl(url) }
    }

    private fun injectBridgeScript() {
        val js = """
            (function() {
              if (window.__gullyBridgeInstalled) { return; }
              window.__gullyBridgeInstalled = true;
              window.addEventListener('message', function(event) {
                try {
                  var data = JSON.parse(event.data);
                  if (data.event === 'onStateChange') {
                    if (data.info == 0) { AndroidBridge.onEnded(); }
                    else if (data.info == 1) { AndroidBridge.onStateChanged('playing'); }
                    else if (data.info == 2) { AndroidBridge.onStateChanged('paused'); }
                  }
                  if (data.event === 'onError') { AndroidBridge.onError(data.info); }
                } catch (e) {}
              });
              setTimeout(function() {
                window.postMessage(JSON.stringify({event:'command', func:'unMute', args:[]}), '*');
                window.postMessage(JSON.stringify({event:'command', func:'setVolume', args:[100]}), '*');
                window.postMessage(JSON.stringify({event:'command', func:'playVideo', args:[]}), '*');
                AndroidBridge.onReady();
              }, 800);
            })();
        """.trimIndent()
        mainHandler.post { webView.evaluateJavascript(js, null) }
    }

    private fun postCommand(func: String) {
        val js = "window.postMessage(JSON.stringify({event:'command', func:'$func', args:[]}), '*');"
        mainHandler.post { webView.evaluateJavascript(js, null) }
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun onReady() {
            mainHandler.post { listener.onPlayerReady() }
        }

        @JavascriptInterface
        fun onStateChanged(state: String) {
            mainHandler.post {
                if (state == "playing") listener.onPlaying() else listener.onPaused()
            }
        }

        @JavascriptInterface
        fun onEnded() {
            mainHandler.post { listener.onSongEnded() }
        }

        @JavascriptInterface
        fun onError(code: Int) {
            val message = when (code) {
                2 -> "Invalid video ID"
                5 -> "This video can't be played in an embedded player"
                100 -> "Video not found — it may have been removed or made private"
                101, 150 -> "The video's owner has disabled playback outside YouTube"
                else -> "Playback error (code $code)"
            }
            mainHandler.post { listener.onError(message) }
        }
    }
}
