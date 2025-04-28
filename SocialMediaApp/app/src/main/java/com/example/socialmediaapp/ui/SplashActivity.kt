package com.example.socialmediaapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Simulate loading resources or checking authentication
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
