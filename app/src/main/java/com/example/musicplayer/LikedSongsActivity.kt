package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.LikedSong
import kotlinx.coroutines.launch
import java.io.IOException

class LikedSongsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var database: AppDatabase
    private var userId: Int = -1
    
    // Media playback components
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var handler: Handler
    private var currentSongIndex = -1
    private var likedSongs = listOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked_songs)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Liked Songs"

        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Initialize media player
        mediaPlayer = MediaPlayer()
        handler = Handler(Looper.getMainLooper())
        
        // Initialize UI components
        seekBar = findViewById(R.id.seekBar)
        setupSeekBarListener()

        // Get current user ID
        val sharedPreferences = getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.likedSongsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        songsAdapter = SongsAdapter(
            emptyList(),
            { song -> 
                // Handle song click - Play the song directly
                playSong(song)
            },
            { song, isLiked ->
                // Handle like button click
                handleLikeButtonClick(song, isLiked)
            },
            { song ->
                // Handle delete button click
                handleDeleteSong(song)
            }
        )
        recyclerView.adapter = songsAdapter

        // Load liked songs
        loadLikedSongs()

        // Set up back button
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        
        // Set up player controls
        val pauseResumeButton: Button = findViewById(R.id.pauseResumeButton)
        pauseResumeButton.setOnClickListener {
            togglePlayback()
        }
        
        val previousButton: Button = findViewById(R.id.previousButton)
        previousButton.setOnClickListener {
            playPreviousSong()
        }
        
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            playNextSong()
        }
        
        // Set up player back button
        val playerBackButton: Button = findViewById(R.id.playerBackButton)
        playerBackButton.setOnClickListener {
            showSongsList()
        }
        
        // Set up player close button
        val playerCloseButton: Button = findViewById(R.id.playerCloseButton)
        playerCloseButton.setOnClickListener {
            showSongsList()
        }
        
        // Set up media player listeners
        mediaPlayer.setOnPreparedListener {
            it.start()
            seekBar.max = it.duration
            updateSeekBar()
            updatePlayPauseButton()
        }
        
        mediaPlayer.setOnCompletionListener {
            playNextSong()
        }
    }
    
    private fun showSongsList() {
        findViewById<CardView>(R.id.Playing_Song_Cardview).visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    private fun showNowPlaying() {
        findViewById<CardView>(R.id.Playing_Song_Cardview).visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun loadLikedSongs() {
        lifecycleScope.launch {
            val dbLikedSongs = database.likedSongDao().getLikedSongsByUser(userId)
            
            // Convert LikedSong entities to Song objects
            likedSongs = dbLikedSongs.map { likedSong ->
                Song(
                    title = likedSong.title,
                    artist = likedSong.artist,
                    path = likedSong.path,
                    albumArtUri = likedSong.albumArtUri,
                    isPlaying = false,
                    isLiked = true
                )
            }
            
            // Update UI on main thread
            runOnUiThread {
                if (likedSongs.isEmpty()) {
                    findViewById<TextView>(R.id.emptyStateTextView).visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    findViewById<TextView>(R.id.emptyStateTextView).visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    songsAdapter.submitList(likedSongs)
                }
            }
        }
    }

    private fun handleLikeButtonClick(song: Song, isLiked: Boolean) {
        lifecycleScope.launch {
            if (!isLiked) {
                // Remove from liked songs
                database.likedSongDao().unlikeSong(userId, song.path)
                
                // Refresh the list
                loadLikedSongs()
            }
        }
    }

    private fun handleDeleteSong(song: Song) {
        // Show confirmation dialog before deleting
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Song")
        builder.setMessage("Are you sure you want to remove '${song.title}' from your liked songs?")
        builder.setPositiveButton("Remove") { _, _ ->
            lifecycleScope.launch {
                // Remove from liked songs in database
                val userId = getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
                    .getInt("user_id", -1)
                
                if (userId != -1) {
                    // Unlike the song in database
                    database.likedSongDao().unlikeSong(userId, song.path)
                    
                    // If the deleted song is currently playing, play the next song
                    if (currentSongIndex >= 0 && currentSongIndex < likedSongs.size && 
                        likedSongs[currentSongIndex].path == song.path) {
                        playNextSong()
                    }
                    
                    // Refresh the list
                    loadLikedSongs()
                    
                    runOnUiThread {
                        Toast.makeText(
                            this@LikedSongsActivity,
                            "Song removed from liked songs",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    private fun playSong(song: Song) {
        try {
            // Reset previous playing state
            if (currentSongIndex >= 0 && currentSongIndex < likedSongs.size) {
                likedSongs[currentSongIndex].isPlaying = false
                songsAdapter.notifyItemChanged(currentSongIndex)
            }
            
            // Set new playing state
            currentSongIndex = likedSongs.indexOf(song)
            if (currentSongIndex >= 0) {
                likedSongs[currentSongIndex].isPlaying = true
                songsAdapter.notifyItemChanged(currentSongIndex)
            }
            
            // Reset and prepare media player
            mediaPlayer.reset()
            mediaPlayer.setDataSource(song.path)
            mediaPlayer.prepareAsync()
            
            // Update UI
            findViewById<TextView>(R.id.song_title).text = song.title
            findViewById<TextView>(R.id.song_artist).text = song.artist
            
            // Update album art
            val albumImageView = findViewById<ImageView>(R.id.Playing_Song_Imageview)
            Glide.with(this)
                .load(song.albumArtUri)
                .placeholder(R.drawable.audioicon)
                .error(R.drawable.audioicon)
                .into(albumImageView)
            
            // Show now playing view
            showNowPlaying()
            
        } catch (e: IOException) {
            Toast.makeText(this, "Error playing song: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun playNextSong() {
        if (likedSongs.isEmpty()) return
        
        if (currentSongIndex < 0) {
            currentSongIndex = 0
        } else {
            currentSongIndex = (currentSongIndex + 1) % likedSongs.size
        }
        
        playSong(likedSongs[currentSongIndex])
    }
    
    private fun playPreviousSong() {
        if (likedSongs.isEmpty()) return
        
        if (currentSongIndex <= 0) {
            currentSongIndex = likedSongs.size - 1
        } else {
            currentSongIndex--
        }
        
        playSong(likedSongs[currentSongIndex])
    }
    
    private fun togglePlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        updatePlayPauseButton()
    }
    
    private fun updatePlayPauseButton() {
        val pauseResumeButton = findViewById<Button>(R.id.pauseResumeButton)
        if (mediaPlayer.isPlaying) {
            pauseResumeButton.setBackgroundResource(R.drawable.pause)
        } else {
            pauseResumeButton.setBackgroundResource(R.drawable.play)
        }
    }
    
    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            seekBar.progress = mediaPlayer.currentPosition
            
            // Update time displays
            val positiveTimer = findViewById<TextView>(R.id.positive_playback_timer)
            val negativeTimer = findViewById<TextView>(R.id.negative_playback_timer)
            
            positiveTimer.text = formatTime(mediaPlayer.currentPosition)
            negativeTimer.text = formatTime(mediaPlayer.duration - mediaPlayer.currentPosition)
            
            // Schedule next update
            handler.postDelayed(this::updateSeekBar, 1000)
        }
    }
    
    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    
                    // Update time displays
                    val positiveTimer = findViewById<TextView>(R.id.positive_playback_timer)
                    val negativeTimer = findViewById<TextView>(R.id.negative_playback_timer)
                    
                    positiveTimer.text = formatTime(progress)
                    negativeTimer.text = formatTime(mediaPlayer.duration - progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })
    }
    
    private fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
            handler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
