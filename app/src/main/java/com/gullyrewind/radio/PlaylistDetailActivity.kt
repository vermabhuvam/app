package com.gullyrewind.radio

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gullyrewind.radio.adapter.SongAdapter
import com.gullyrewind.radio.data.SongRepository

class PlaylistDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        val playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID)
        val playlist = SongRepository.playlists.firstOrNull { it.id == playlistId }
            ?: SongRepository.playlists.first()

        findViewById<TextView>(R.id.tvPlaylistTitle).text = playlist.title
        findViewById<TextView>(R.id.tvPlaylistTagline).text = playlist.tagline

        val songs = SongRepository.songsFor(playlist.category)

        val recycler = findViewById<RecyclerView>(R.id.recyclerPlaylistSongs)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = SongAdapter(songs) { song ->
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra(MainActivity.EXTRA_CATEGORY, playlist.category)
            intent.putExtra(MainActivity.EXTRA_SONG_ID, song.id)
            startActivity(intent)
        }
    }
}
