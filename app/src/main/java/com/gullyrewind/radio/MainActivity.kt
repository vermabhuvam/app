package com.gullyrewind.radio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gullyrewind.radio.data.SongRepository
import com.gullyrewind.radio.model.Song
import com.gullyrewind.radio.player.YouTubeAudioPlayer
import kotlin.random.Random

class MainActivity : AppCompatActivity(), YouTubeAudioPlayer.Listener {

    companion object {
        const val EXTRA_SONG_ID = "extra_song_id"
        const val EXTRA_CATEGORY = "extra_category"
    }

    private lateinit var player: YouTubeAudioPlayer
    private var queue: List<Song> = SongRepository.songs
    private var currentIndex = 0
    private var isPlaying = false

    private lateinit var tvSongTitle: TextView
    private lateinit var tvSongMeta: TextView
    private lateinit var tvListeners: TextView
    private lateinit var btnPlayPause: ImageButton

    private val listenerHandler = Handler(Looper.getMainLooper())
    private var simulatedListeners = Random.nextInt(40, 180)

    private val listenerTicker = object : Runnable {
        override fun run() {
            // Simulated local listener count. To make this a real, cross-device
            // live number you'd replace this with a small backend (e.g. a
            // Firebase Realtime Database counter or a lightweight WebSocket
            // service) that every installed app connects to.
            simulatedListeners = (simulatedListeners + Random.nextInt(-3, 4)).coerceIn(20, 400)
            tvListeners.text = getString(R.string.listening_now, simulatedListeners)
            listenerHandler.postDelayed(this, 4000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvSongMeta = findViewById(R.id.tvSongMeta)
        tvListeners = findViewById(R.id.tvListeners)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        val webView = findViewById<WebView>(R.id.webViewPlayer)

        resolveInitialQueueAndIndex(intent)

        player = YouTubeAudioPlayer(webView, this)
        player.initialize(queue[currentIndex].youtubeId)
        renderCurrentSong()

        btnPlayPause.setOnClickListener {
            if (isPlaying) player.pause() else player.resume()
        }
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener { playAt(currentIndex + 1) }
        findViewById<ImageButton>(R.id.btnPrevious).setOnClickListener { playAt(currentIndex - 1) }

        findViewById<TextView>(R.id.btnPlaylists).setOnClickListener {
            startActivity(Intent(this, PlaylistsActivity::class.java))
        }
        findViewById<TextView>(R.id.btnAllSongs).setOnClickListener {
            startActivity(Intent(this, SongsActivity::class.java))
        }
        findViewById<TextView>(R.id.btnWhatsapp).setOnClickListener {
            openUrl("https://whatsapp.com/channel/REPLACE_WITH_YOUR_CHANNEL_LINK")
        }
        findViewById<TextView>(R.id.btnSpotify).setOnClickListener {
            openUrl("https://open.spotify.com/")
        }
        findViewById<TextView>(R.id.btnYtMusic).setOnClickListener {
            openUrl("https://music.youtube.com/")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveInitialQueueAndIndex(intent)
        playAt(currentIndex)
    }

    private fun resolveInitialQueueAndIndex(intent: Intent?) {
        val category = intent?.getStringExtra(EXTRA_CATEGORY)
        val songId = intent?.getStringExtra(EXTRA_SONG_ID)

        queue = if (category != null) SongRepository.songsFor(category) else SongRepository.songs
        if (queue.isEmpty()) queue = SongRepository.songs

        currentIndex = if (songId != null) {
            queue.indexOfFirst { it.id == songId }.coerceAtLeast(0)
        } else {
            0
        }
    }

    private fun playAt(index: Int) {
        if (queue.isEmpty()) return
        currentIndex = ((index % queue.size) + queue.size) % queue.size
        player.play(queue[currentIndex].youtubeId)
        renderCurrentSong()
    }

    private fun renderCurrentSong() {
        val song = queue[currentIndex]
        tvSongTitle.text = song.title
        tvSongMeta.text = "${song.movie} · ${song.year}"
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't open link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        listenerHandler.post(listenerTicker)
    }

    override fun onPause() {
        super.onPause()
        listenerHandler.removeCallbacks(listenerTicker)
    }

    // --- YouTubeAudioPlayer.Listener ---

    override fun onPlaying() {
        isPlaying = true
        btnPlayPause.setImageResource(R.drawable.ic_pause)
    }

    override fun onPaused() {
        isPlaying = false
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    override fun onSongEnded() {
        playAt(currentIndex + 1)
    }

    override fun onPlayerReady() {
        isPlaying = true
        btnPlayPause.setImageResource(R.drawable.ic_pause)
    }
}
