package com.example.socialmediaapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.data.Post
import com.example.socialmediaapp.repository.PostRepository
import com.example.socialmediaapp.utils.SessionManager

class CreatePostActivity : AppCompatActivity() {
    private lateinit var postRepository: PostRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postRepository = PostRepository()
        sessionManager = SessionManager(this)

        val postContentInput = findViewById<EditText>(R.id.post_content)
        val createPostButton = findViewById<Button>(R.id.create_post_button)

        createPostButton.setOnClickListener {
            val content = postContentInput.text.toString()
            if (content.isNotEmpty()) {
                val userId = sessionManager.getUserId() ?: "unknown_user"
                val newPost = Post("post_id_example", userId, content, null, System.currentTimeMillis())
                postRepository.addPost(newPost)
                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            } else {
                Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
            }
        }
    }
    }
}
