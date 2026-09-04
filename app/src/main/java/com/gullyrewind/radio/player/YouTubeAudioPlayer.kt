package com.gullyrewind.radio.player

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * Plays audio from a YouTube video inside a hidden WebView, using YouTube's
 * official IFrame Player API. Nothing is downloaded or re-hosted — playback
 * is a live embed exactly like the <iframe> YouTube gives any site, which
 * keeps this within YouTube's Terms of Service.
 *
 * Two things mobile WebViews commonly need for this to actually make sound:
 *  1. Browsers block autoplay of unmuted media unless it starts muted and is
 *     unmuted right after playback begins — so we do exactly that.
 *  2. If the specific video disallows embedding, YouTube fires an error
 *     event instead of playing — we surface that back to the app instead of
 *     failing silently, via [Listener.onError].
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
        WebView.setWebContentsDebuggingEnabled(true) // lets you inspect via chrome://inspect
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
        webView.loadDataWithBaseURL(
            "https://www.youtube.com",
            buildHtml(firstVideoId),
            "text/html",
            "utf-8",
            null
        )
    }

    fun play(videoId: String) {
        mainHandler.post {
            webView.evaluateJavascript("playVideoById('$videoId')", null)
        }
    }

    fun resume() {
        mainHandler.post { webView.evaluateJavascript("resumeVideoNow()", null) }
    }

    fun pause() {
        mainHandler.post { webView.evaluateJavascript("pauseVideoNow()", null) }
    }

    private fun buildHtml(videoId: String) = """
        <!DOCTYPE html>
        <html>
        <head><style>body{margin:0;background:transparent;}</style></head>
        <body>
        <div id="player"></div>
        <script src="https://www.youtube.com/iframe_api"></script>
        <script>
          var player;
          var unmuteTimer;
          function onYouTubeIframeAPIReady() {
            player = new YT.Player('player', {
              height: '200',
              width: '200',
              videoId: '$videoId',
              playerVars: { 'autoplay': 1, 'playsinline': 1, 'controls': 0, 'mute': 1 },
              events: {
                'onReady': onPlayerReady,
                'onStateChange': onPlayerStateChange,
                'onError': onPlayerError
              }
            });
          }
          function onPlayerReady(e) {
            e.target.mute();
            e.target.playVideo();
            // Autoplay is only reliably allowed muted, so start muted then
            // unmute right after playback actually begins.
            clearTimeout(unmuteTimer);
            unmuteTimer = setTimeout(function() {
              e.target.unMute();
              e.target.setVolume(100);
              AndroidBridge.onReady();
            }, 600);
          }
          function onPlayerStateChange(event) {
            if (event.data == YT.PlayerState.ENDED) { AndroidBridge.onEnded(); }
            if (event.data == YT.PlayerState.PLAYING) { AndroidBridge.onStateChanged('playing'); }
            if (event.data == YT.PlayerState.PAUSED) { AndroidBridge.onStateChanged('paused'); }
          }
          function onPlayerError(event) {
            AndroidBridge.onError(event.data);
          }
          function playVideoById(id) {
            if (player && player.loadVideoById) {
              player.mute();
              player.loadVideoById(id);
              clearTimeout(unmuteTimer);
              unmuteTimer = setTimeout(function() {
                player.unMute();
                player.setVolume(100);
              }, 600);
            }
          }
          function pauseVideoNow() { if (player && player.pauseVideo) { player.pauseVideo(); } }
          function resumeVideoNow() { if (player && player.playVideo) { player.playVideo(); } }
        </script>
        </body>
        </html>
    """.trimIndent()

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
                5 -> "This video can't be played in an embedded player (HTML5 error)"
                100 -> "Video not found — it may have been removed or made private"
                101, 150 -> "The video's owner has disabled playback outside YouTube"
                else -> "Unknown playback error (code $code)"
            }
            mainHandler.post { listener.onError(message) }
        }
    }
}
