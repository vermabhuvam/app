package com.gullyrewind.radio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gullyrewind.radio.adapter.PlaylistAdapter
import com.gullyrewind.radio.data.SongRepository

class PlaylistsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        val recycler = findViewById<RecyclerView>(R.id.recyclerPlaylists)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = PlaylistAdapter(SongRepository.playlists) { playlist ->
            val intent = Intent(this, PlaylistDetailActivity::class.java)
            intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_ID, playlist.id)
            startActivity(intent)
        }
    }
}
