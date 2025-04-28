package com.example.socialmediaapp.repository

import com.example.socialmediaapp.data.Post

class PostRepository {
    private val posts = mutableListOf<Post>()

    fun getPosts(): List<Post> {
        return posts
    }

    fun addPost(post: Post) {
        posts.add(post)
    }
}
