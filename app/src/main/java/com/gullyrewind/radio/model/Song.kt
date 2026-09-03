package com.gullyrewind.radio.model

/**
 * A single track in the radio.
 *
 * [youtubeId] is the 11-character ID from a YouTube URL, e.g.
 * for https://www.youtube.com/watch?v=cNV5hLSa9H8 the id is "cNV5hLSa9H8".
 * Audio is streamed live through YouTube's own embedded player — nothing
 * is downloaded or re-hosted, matching YouTube's Terms of Service.
 */
data class Song(
    val id: String,
    val title: String,
    val movie: String,
    val singers: String,
    val year: Int,
    val youtubeId: String,
    val category: String
)
