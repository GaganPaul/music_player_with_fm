package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Set up Get Started button
        val getStartedButton: Button = findViewById(R.id.getStartedButton)
        getStartedButton.setOnClickListener {
            // Check if user is already logged in
            val sharedPreferences = getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getInt("user_id", -1) != -1
            
            if (isLoggedIn) {
                // If user is logged in, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If user is not logged in, go to login screen
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
    }
}
