package com.example.musicplayer

import android.content.Intent
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)
        
        // Set up password visibility toggles
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
        
        val toggleConfirmPasswordVisibility: ImageButton = findViewById(R.id.toggleConfirmPasswordVisibility)
        var confirmPasswordVisible = false
        
        toggleConfirmPasswordVisibility.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            
            if (confirmPasswordVisible) {
                // Show password
                confirmPasswordEditText.transformationMethod = null
                toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                // Hide password
                confirmPasswordEditText.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_view)
            }
            // Move cursor to the end of text
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        // Set click listeners
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate email format
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(username, password, email)
        }

        loginTextView.setOnClickListener {
            finish() // Go back to login screen
        }
    }
    
    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun registerUser(username: String, password: String, email: String) {
        lifecycleScope.launch {
            val existingUser = database.userDao().getUserByUsername(username)
            if (existingUser != null) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Username already exists",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Create new user
            val newUser = User(
                username = username,
                password = password,
                email = email
            )

            // Insert user into database
            database.userDao().insertUser(newUser)

            Toast.makeText(
                this@RegisterActivity,
                "Registration successful! Please login.",
                Toast.LENGTH_SHORT
            ).show()

            // Return to login screen
            finish()
        }
    }
}
