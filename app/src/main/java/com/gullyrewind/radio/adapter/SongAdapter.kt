package com.gullyrewind.radio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gullyrewind.radio.R
import com.gullyrewind.radio.model.Song

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClicked: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemSongTitle)
        val meta: TextView = itemView.findViewById(R.id.tvItemSongMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.meta.text = "${song.movie} · ${song.year}"
        holder.itemView.setOnClickListener { onSongClicked(song) }
    }

    override fun getItemCount(): Int = songs.size
}
