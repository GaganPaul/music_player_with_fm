package com.example.musicplayer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.PlaybackException

class RadioPlayer(private val context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    
    private var playbackStateListener: PlaybackStateListener? = null
    private var currentUrl: String? = null
    private var retryCount = 0
    private val maxRetries = 3
    
    init {
        setupPlayerListeners()
    }
    
    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("RadioPlayer", "Error playing stream: ${error.message}")
                
                if (retryCount < maxRetries) {
                    retryCount++
                    Log.d("RadioPlayer", "Retrying playback (attempt $retryCount)")
                    retry()
                } else {
                    retryCount = 0
                    playbackStateListener?.onError(error.message ?: "Unknown error")
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d("RadioPlayer", "Ready to play")
                        retryCount = 0
                        playbackStateListener?.onReady()
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d("RadioPlayer", "Buffering")
                        playbackStateListener?.onBuffering()
                    }
                    Player.STATE_ENDED -> {
                        Log.d("RadioPlayer", "Playback ended")
                        retry() // Auto retry when stream ends
                    }
                    Player.STATE_IDLE -> {
                        Log.d("RadioPlayer", "Player idle")
                    }
                }
            }
        })
    }

    fun play(url: String) {
        try {
            currentUrl = url
            Log.d("RadioPlayer", "Attempting to play: $url")
            
            // Create MediaItem from URL
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            Log.e("RadioPlayer", "Exception when setting up playback: ${e.message}")
            playbackStateListener?.onError(e.message ?: "Unknown error")
        }
    }
    
    fun retry() {
        currentUrl?.let { play(it) }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        player.release()
    }
    
    fun isPlaying(): Boolean {
        return player.isPlaying
    }
    
    fun setPlaybackStateListener(listener: PlaybackStateListener) {
        this.playbackStateListener = listener
    }
    
    interface PlaybackStateListener {
        fun onReady()
        fun onBuffering()
        fun onError(errorMessage: String)
    }
}