package com.example.socialmediaapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.utils.SessionManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val usernameTextView = findViewById<TextView>(R.id.username)
        val bioTextView = findViewById<TextView>(R.id.bio)
        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        val editProfileButton = findViewById<Button>(R.id.edit_profile_button)

        // Simulate fetching user data
        val userId = sessionManager.getUserId() ?: "unknown_user"
        usernameTextView.text = "User: $userId"
        bioTextView.text = "This is the user's bio."

        editProfileButton.setOnClickListener {
            // Handle edit profile action
        }
    }
    }
}
