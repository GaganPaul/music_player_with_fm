package com.example.musicplayer.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LikedSongDao {
    @Insert
    suspend fun insertLikedSong(likedSong: LikedSong)
    
    @Query("SELECT * FROM liked_songs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getLikedSongsByUser(userId: Int): List<LikedSong>
    
    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE userId = :userId AND path = :songPath LIMIT 1)")
    suspend fun isSongLiked(userId: Int, songPath: String): Boolean
    
    @Query("DELETE FROM liked_songs WHERE userId = :userId AND path = :songPath")
    suspend fun unlikeSong(userId: Int, songPath: String)
}
