package com.example.socialmediaapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaapp.data.Post

class PostDetailActivity : AppCompatActivity() {
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Simulate fetching post data
        post = Post("1", "user_id_example", "Hello World!", null, System.currentTimeMillis())

        val postContentTextView = findViewById<TextView>(R.id.post_content)
        val postImageView = findViewById<ImageView>(R.id.post_image)
        val likeButton = findViewById<Button>(R.id.like_button)
        val commentButton = findViewById<Button>(R.id.comment_button)

        postContentTextView.text = post.content
        // Load image into postImageView if available

        likeButton.setOnClickListener {
            // Handle like action
        }

        commentButton.setOnClickListener {
            // Handle comment action
        }
    }
    }
}
