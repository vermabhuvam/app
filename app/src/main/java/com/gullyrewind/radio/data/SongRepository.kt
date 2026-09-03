package com.gullyrewind.radio.data

import com.gullyrewind.radio.model.Playlist
import com.gullyrewind.radio.model.Song

/**
 * In-app song + playlist database.
 *
 * Every entry below was verified against a real YouTube upload before being
 * added here, so the radio actually plays out of the box.
 *
 * TO ADD MORE SONGS:
 *   1. Find the song's official video on YouTube.
 *   2. Copy the 11-character id from the URL after "v=".
 *   3. Add a new Song(...) line below with that id.
 * That's it — no other code changes needed.
 */
object SongRepository {

    const val CATEGORY_ROMANTIC = "romantic"
    const val CATEGORY_WEDDING = "wedding"
    const val CATEGORY_SAD = "sad"

    val playlists = listOf(
        Playlist(
            id = "pl_romantic",
            title = "Gully Ki Mohabbat",
            tagline = "90s love songs for a slow evening",
            category = CATEGORY_ROMANTIC
        ),
        Playlist(
            id = "pl_wedding",
            title = "Baraat Anthem",
            tagline = "Wedding-season dance floor classics",
            category = CATEGORY_WEDDING
        ),
        Playlist(
            id = "pl_sad",
            title = "Dard Bhare Nagme",
            tagline = "For when the heart needs company",
            category = CATEGORY_SAD
        )
    )

    val songs = listOf(
        Song(
            id = "s1",
            title = "Tujhe Dekha Toh",
            movie = "Dilwale Dulhania Le Jayenge",
            singers = "Lata Mangeshkar, Kumar Sanu",
            year = 1995,
            youtubeId = "cNV5hLSa9H8",
            category = CATEGORY_ROMANTIC
        ),
        Song(
            id = "s2",
            title = "Tip Tip Barsa Paani",
            movie = "Mohra",
            singers = "Udit Narayan, Alka Yagnik",
            year = 1994,
            youtubeId = "HyKuXycQXkg",
            category = CATEGORY_ROMANTIC
        ),
        Song(
            id = "s3",
            title = "Chura Ke Dil Mera",
            movie = "Main Khiladi Tu Anari",
            singers = "Kumar Sanu, Alka Yagnik",
            year = 1994,
            youtubeId = "1eSG6dLiYxY",
            category = CATEGORY_WEDDING
        ),
        Song(
            id = "s4",
            title = "Mehndi Laga Ke Rakhna",
            movie = "Dilwale Dulhania Le Jayenge",
            singers = "Lata Mangeshkar, Udit Narayan",
            year = 1995,
            youtubeId = "-bNwqXvMuB8",
            category = CATEGORY_WEDDING
        ),
        Song(
            id = "s5",
            title = "Chaha Hai Tujhko",
            movie = "Mann",
            singers = "Udit Narayan, Anuradha Paudwal",
            year = 1999,
            youtubeId = "syh23tsuGFs",
            category = CATEGORY_SAD
        )
    )

    fun songsFor(category: String): List<Song> = songs.filter { it.category == category }
}
