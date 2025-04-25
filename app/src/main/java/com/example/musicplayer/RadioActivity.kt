package com.example.musicplayer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RadioActivity : AppCompatActivity(), RadioPlayer.PlaybackStateListener {
    private lateinit var radioPlayer: RadioPlayer
    private lateinit var adapter: RadioStationAdapter
    private lateinit var nowPlayingCard: CardView
    private lateinit var currentStationImage: ImageView
    private lateinit var currentStationName: TextView
    private lateinit var currentStationCategory: TextView
    private lateinit var playPauseButton: Button
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private var currentPlayingStation: RadioStation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio)
        
        // Initialize player
        radioPlayer = RadioPlayer(this)
        radioPlayer.setPlaybackStateListener(this)
        
        // Setup UI components
        val recyclerView: RecyclerView = findViewById(R.id.radioRecyclerView)
        nowPlayingCard = findViewById(R.id.nowPlayingCard)
        currentStationImage = findViewById(R.id.currentStationImage)
        currentStationName = findViewById(R.id.currentStationName)
        currentStationCategory = findViewById(R.id.currentStationCategory)
        playPauseButton = findViewById(R.id.playPauseButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        emptyStateText = findViewById(R.id.emptyStateText)
        
        // Check for network connectivity
        if (!isNetworkAvailable()) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            Toast.makeText(
                this,
                "No internet connection. Radio streaming requires internet.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        
        // Back button
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Set up the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Create list of radio stations with more reliable streams
        val radioStations = listOf(
            // US Stations
            RadioStation("Ilayaraja FM", "", "https://stream-161.zeno.fm/pwm9y3eud5zuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJwd205eTNldWQ1enV2IiwiaG9zdCI6InN0cmVhbS0xNjEuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6IlE3NVgtNGdGVFkyWHV0WDM3aEN0eWciLCJpYXQiOjE3NDQ1NTM4NzAsImV4cCI6MTc0NDU1MzkzMH0.fw3E-ogBhVoNp5UiypYX110mNtwoN5sgbkYTpeHPn8s", "music", true),
            RadioStation("K.J.Yesudas Radio", "", "https://stream-157.zeno.fm/trtwuyqdmrgtv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJ0cnR3dXlxZG1yZ3R2IiwiaG9zdCI6InN0cmVhbS0xNTcuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6IkxNRmxqZFN5UjctVnk1cWxOb210bFEiLCJpYXQiOjE3NDQ1NTQxMzQsImV4cCI6MTc0NDU1NDE5NH0.fEGVZotllP786cBaHp5F9d_alFLOW-fR6oztj8TJT24", "music", true),
            RadioStation("S.P.Balasubrahmanyam Radio", "", "https://stream-157.zeno.fm/9adkzdcqi4jvv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiI5YWRremRjcWk0anZ2IiwiaG9zdCI6InN0cmVhbS0xNTcuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6ImhieG45Y0JRUk95NzAtX1dsTWhGQlEiLCJpYXQiOjE3NDQ1NTQzNzUsImV4cCI6MTc0NDU1NDQzNX0.ty0T_7NvO1VtBaqPk83U9drVPxAimPsM_G5nYoPJEe4", "music", true),
            RadioStation("KS Chitra Radio", "", "https://stream-162.zeno.fm/aad4e51qz7zuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJhYWQ0ZTUxcXo3enV2IiwiaG9zdCI6InN0cmVhbS0xNjIuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6ImpSUUVYdmE3VE5DMlVJZ1pUSVI2bUEiLCJpYXQiOjE3NDQ1NTQ1OTgsImV4cCI6MTc0NDU1NDY1OH0.R31V_IV1LfLXNaxEdoi0aI3mF-pS0KivzPzj2CT1Rw0", "music", true),
            RadioStation("Harris Jayaraj Radio", "", "https://stream-154.zeno.fm/0bhsthssutzuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiIwYmhzdGhzc3V0enV2IiwiaG9zdCI6InN0cmVhbS0xNTQuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6InRCRU1fSTA5U01TWEF2WmtlWkdmRVEiLCJpYXQiOjE3NDQ1NTYzMTQsImV4cCI6MTc0NDU1NjM3NH0.14sfHFu_F2Mkr7uc0CLA1lm_Q6AOQz4qYV82kMh6URg", "music", true),
            RadioStation("GV Prakash", "", "https://stream-174.zeno.fm/d5skwct7hqzuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJkNXNrd2N0N2hxenV2IiwiaG9zdCI6InN0cmVhbS0xNzQuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6InBLYm9YOXJLUlF1TF9fTl9FeGJBWkEiLCJpYXQiOjE3NDQ1NTcwMjEsImV4cCI6MTc0NDU1NzA4MX0.6Swxxq8Md99ZwcQN0ec5jITNPh0NUuraqutTrI143J0", "music", true),
            RadioStation("Yuvan Shankar Raja radio FM", "", "https://stream-162.zeno.fm/aad4e51qz7zuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJhYWQ0ZTUxcXo3enV2IiwiaG9zdCI6InN0cmVhbS0xNjIuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6ImpSUUVYdmE3VE5DMlVJZ1pUSVI2bUEiLCJpYXQiOjE3NDQ1NTQ1OTgsImV4cCI6MTc0NDU1NDY1OH0.R31V_IV1LfLXNaxEdoi0aI3mF-pS0KivzPzj2CT1Rw0", "music", true),
            RadioStation("Kannada Radio", "", "https://stream-157.zeno.fm/68snnbug8rhvv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiI2OHNubmJ1ZzhyaHZ2IiwiaG9zdCI6InN0cmVhbS0xNTcuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6IjlRVVh5Ykl1U2stS2YwRjNsRzZwdGciLCJpYXQiOjE3NDQ1NTYxMjcsImV4cCI6MTc0NDU1NjE4N30.wNL5Mzp777v0WpKNoMgPH34armHMaeRuamQ4rdyCPsU", "music", true),
            RadioStation("NPR News", "https://media.npr.org/assets/img/2022/04/29/npr-logo_rgb_300dpi_custom-fc2b0c797e82d6a32ba44d7fb64557102f9c3b60-s1100-c50.jpg", "https://npr-ice.streamguys1.com/live.mp3", "News", true),
            RadioStation("Smooth Jazz", "https://smoothjazz.com/wp-content/uploads/2016/07/smooth-jazz-logo-small.png", "https://smoothjazz.cdnstream1.com/2585_320.mp3", "Jazz (Songs)", true),
            RadioStation("Tamil Retro FM", "", "https://spserver.sscast2u.in/retrofmtamil/stream", "music", true),
            RadioStation("Malayalam hits", "", "https://stream-155.zeno.fm/5vva7u4ts0quv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiI1dnZhN3U0dHMwcXV2IiwiaG9zdCI6InN0cmVhbS0xNTUuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6ImRGMDlzeTBPVEh5dl94czU4bFlmLWciLCJpYXQiOjE3NDQ1NTc5NTMsImV4cCI6MTc0NDU1ODAxM30.meaA0RMpM4bnLwA2qOv42yQ5zWzsqPtFszVuJ8iFa_w", "music", true),
            RadioStation("Bharat FM Hindi", "", "https://s2.radio.co/s44bb88930/listen", "music", true),
            RadioStation("BBC World Service", "https://ichef.bbci.co.uk/images/ic/1200x675/p08b4gw6.jpg", "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service", "News", true),
            RadioStation("Bollywood Hits", "", "https://strm112.1.fm/bombaybeats_mobile_mp3", "Culture (Songs)", false),
            RadioStation("Lofi Hip Hop", "", "https://streams.ilovemusic.de/iloveradio17.mp3", "Chillout", true),
            RadioStation("Namm Radio", "", "https://stream-153.zeno.fm/6quh1pfnt1duv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiI2cXVoMXBmbnQxZHV2IiwiaG9zdCI6InN0cmVhbS0xNTMuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6ImNIdDhyUG1NUl9XSFV6QWd6cWRyM1EiLCJpYXQiOjE3NDQ1NTEzMzUsImV4cCI6MTc0NDU1MTM5NX0.xV4enpz12o5KREBlN9YSmdL5uHFC8mf7iBo10VB88lo", "Culture (Songs)", true),
            RadioStation("92.7 Big FM ", "", "https://stream-155.zeno.fm/dbstwo3dvhhtv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJkYnN0d28zZHZoaHR2IiwiaG9zdCI6InN0cmVhbS0xNTUuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6IjRXYkp5OHVFVFFDc2FSRnRTX2dQZ2ciLCJpYXQiOjE3NDQ1NTI1NDAsImV4cCI6MTc0NDU1MjYwMH0.enL3kdw8Z0Szo5lR_Fc_Dl66qcvA639RPY46lu9mv9s", "indian music", true),
            RadioStation("Mirchi Masala Radio Station", "", "https://streaming.brol.tech/rtfmlounge", "music", true),
            RadioStation("Tamil Radio", "", "https://tamilip.com:8345/;", "Devotional", true),
            RadioStation("OM Radio", "", "https://playerservices.streamtheworld.com/api/livestream-redirect/OMRADIO_S01AAC_SC", "Devotional", true),
            RadioStation("Sivan Kovil Bakthi FM", "", "https://stream-159.zeno.fm/hm9s230a4qzuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJobTlzMjMwYTRxenV2IiwiaG9zdCI6InN0cmVhbS0xNTkuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6InoyT01kNEVQUVl1RjR2Z0FSUUprVnciLCJpYXQiOjE3NDQ1NTUzMzgsImV4cCI6MTc0NDU1NTM5OH0.ocLVss3Y5-b9rkZE6OkS2wJt_ukRU7UIW5G_oUAGsVc", "music", true),
            RadioStation("Arulvakku FM", "", "https://stream.arulvakku.com/radio/8000/radio.mp3", "music", true)
        )
        
        // Set up the adapter
        adapter = RadioStationAdapter(radioStations) { station ->
            if (isNetworkAvailable()) {
                handleStationClick(station)
            } else {
                Toast.makeText(
                    this,
                    "No internet connection available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        recyclerView.adapter = adapter
        
        // Setup now playing controls
        playPauseButton.setOnClickListener {
            if (isNetworkAvailable()) {
                togglePlayback()
            } else {
                Toast.makeText(
                    this,
                    "No internet connection available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.isConnected == true
        }
    }
    
    private fun handleStationClick(station: RadioStation) {
        if (currentPlayingStation?.streamUrl == station.streamUrl) {
            // If clicking on the already playing station, toggle playback
            togglePlayback()
        } else {
            // Show loading indicator
            loadingIndicator.visibility = View.VISIBLE
            playPauseButton.visibility = View.GONE
            
            // Play the new station
            radioPlayer.play(station.streamUrl)
            currentPlayingStation = station
            updateNowPlayingCard(station)
            
            // Show the now playing card
            nowPlayingCard.visibility = View.VISIBLE
        }
    }
    
    private fun togglePlayback() {
        if (radioPlayer.isPlaying()) {
            radioPlayer.stop()
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play)
        } else {
            loadingIndicator.visibility = View.VISIBLE
            playPauseButton.visibility = View.GONE
            
            currentPlayingStation?.let {
                radioPlayer.play(it.streamUrl)
            }
        }
    }
    
    private fun updateNowPlayingCard(station: RadioStation) {
        currentStationName.text = station.name
        currentStationCategory.text = "Category: ${station.category}"
        
        if (station.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(station.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(currentStationImage)
        } else {
            currentStationImage.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
    
    // RadioPlayer.PlaybackStateListener implementation
    override fun onReady() {
        runOnUiThread {
            loadingIndicator.visibility = View.GONE
            playPauseButton.visibility = View.VISIBLE
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause)
        }
    }
    
    override fun onBuffering() {
        runOnUiThread {
            loadingIndicator.visibility = View.VISIBLE
            playPauseButton.visibility = View.GONE
        }
    }
    
    override fun onError(errorMessage: String) {
        runOnUiThread {
            loadingIndicator.visibility = View.GONE
            playPauseButton.visibility = View.VISIBLE
            playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play)
            
            // Show error message
            Toast.makeText(
                this@RadioActivity,
                "Error playing station: $errorMessage",
                Toast.LENGTH_SHORT
            ).show()
            
            Log.e("RadioActivity", "Playback error: $errorMessage")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        radioPlayer.release()
    }
}

