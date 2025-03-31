package com.example.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSong(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val artist: String,
    val path: String,
    val albumArtUri: String,
    val timestamp: Long = System.currentTimeMillis()
)
