package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.User
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
        
        // Check if user is already logged in
        if (isLoggedIn()) {
            navigateToMainActivity()
            return
        }

        // Initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        
        // Set up password visibility toggle
        val togglePasswordVisibility: ImageButton = findViewById(R.id.togglePasswordVisibility)
        var passwordVisible = false
        
        togglePasswordVisibility.setOnClickListener {
            passwordVisible = !passwordVisible
            
            if (passwordVisible) {
                // Show password
                passwordEditText.transformationMethod = null
                togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                // Hide password
                passwordEditText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_view)
            }
            // Move cursor to the end of text
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Set click listeners
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            val user = database.userDao().getUser(username, password)
            if (user != null) {
                // Save user login state
                saveLoginState(user.id, user.username)
                navigateToMainActivity()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Invalid username or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getInt("user_id", -1) != -1
    }

    private fun saveLoginState(userId: Int, username: String) {
        sharedPreferences.edit().apply {
            putInt("user_id", userId)
            putString("username", username)
            apply()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
