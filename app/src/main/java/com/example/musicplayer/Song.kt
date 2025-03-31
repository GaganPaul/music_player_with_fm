package com.example.musicplayer

data class Song(
    val title: String,
    val artist: String,
    val path: String,
    val albumArtUri: String,
    var isPlaying: Boolean = false, // New property to indicate if the song is currently playing
    var isLiked: Boolean = false // New property to indicate if the song is liked
)
