package com.gullyrewind.radio.player

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * Plays audio from a YouTube video inside a 1x1 hidden WebView, using
 * YouTube's official IFrame Player API. Nothing is downloaded or re-hosted —
 * playback is a live embed exactly like the <iframe> YouTube gives any site,
 * which keeps this within YouTube's Terms of Service.
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
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(firstVideoId: String) {
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")
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
          function onYouTubeIframeAPIReady() {
            player = new YT.Player('player', {
              height: '1',
              width: '1',
              videoId: '$videoId',
              playerVars: { 'autoplay': 1, 'playsinline': 1, 'controls': 0 },
              events: {
                'onReady': function(e) { AndroidBridge.onReady(); e.target.playVideo(); },
                'onStateChange': onPlayerStateChange
              }
            });
          }
          function onPlayerStateChange(event) {
            if (event.data == YT.PlayerState.ENDED) { AndroidBridge.onEnded(); }
            if (event.data == YT.PlayerState.PLAYING) { AndroidBridge.onStateChanged('playing'); }
            if (event.data == YT.PlayerState.PAUSED) { AndroidBridge.onStateChanged('paused'); }
          }
          function playVideoById(id) { if (player && player.loadVideoById) { player.loadVideoById(id); } }
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
    }
}
