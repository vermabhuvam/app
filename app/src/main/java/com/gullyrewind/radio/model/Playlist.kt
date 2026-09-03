package com.gullyrewind.radio.model

data class Playlist(
    val id: String,
    val title: String,
    val tagline: String,
    val category: String // matches Song.category
)
