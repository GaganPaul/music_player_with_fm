package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.database.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserProfileActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        
        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Get current user ID
        val sharedPreferences = getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = sharedPreferences.getString("username", "")
        
        if (userId == -1) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Set up UI components
        val usernameTextView: TextView = findViewById(R.id.usernameTextView)
        val emailTextView: TextView = findViewById(R.id.emailTextView)
        val joinedDateTextView: TextView = findViewById(R.id.joinedDateTextView)
        val backButton: Button = findViewById(R.id.backButton)
        val likedSongsButton: Button = findViewById(R.id.likedSongsButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)
        
        // Load user data
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username ?: "")
            if (user != null) {
                runOnUiThread {
                    usernameTextView.text = user.username
                    emailTextView.text = user.email
                    
                    // Format date (for demo purposes using current date)
                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val currentDate = Date()
                    joinedDateTextView.text = dateFormat.format(currentDate)
                }
            }
        }
        
        // Set click listeners
        backButton.setOnClickListener {
            finish()
        }
        
        likedSongsButton.setOnClickListener {
            startActivity(Intent(this, LikedSongsActivity::class.java))
        }
        
        logoutButton.setOnClickListener {
            // Clear user login state
            sharedPreferences.edit().apply {
                remove("user_id")
                remove("username")
                apply()
            }
            
            // Redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
