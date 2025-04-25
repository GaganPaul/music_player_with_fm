package com.example.musicplayer

data class RadioStation(
    val name: String,
    val imageUrl: String,
    val streamUrl: String,
    val category: String,
    val featured: Boolean
)