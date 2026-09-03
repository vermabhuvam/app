package com.gullyrewind.radio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gullyrewind.radio.R
import com.gullyrewind.radio.model.Playlist

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistClicked: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemPlaylistTitle)
        val tagline: TextView = itemView.findViewById(R.id.tvItemPlaylistTagline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.title.text = playlist.title
        holder.tagline.text = playlist.tagline
        holder.itemView.setOnClickListener { onPlaylistClicked(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}
