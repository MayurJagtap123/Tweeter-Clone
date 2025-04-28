package com.example.socialmediaapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaapp.data.Post
import com.example.socialmediaapp.repository.PostRepository

class MainActivity : AppCompatActivity() {
    private lateinit var postRepository: PostRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        postRepository = PostRepository()
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data
        postRepository.addPost(Post("1", "user_id_example", "Hello World!", null, System.currentTimeMillis()))

        postAdapter = PostAdapter(postRepository.getPosts())
        recyclerView.adapter = postAdapter
    }
    }
}
