package com.gullyrewind.radio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gullyrewind.radio.adapter.SongAdapter
import com.gullyrewind.radio.data.SongRepository

class SongsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        val recycler = findViewById<RecyclerView>(R.id.recyclerSongs)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = SongAdapter(SongRepository.songs) { song ->
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            intent.putExtra(MainActivity.EXTRA_SONG_ID, song.id)
            startActivity(intent)
        }
    }
}
