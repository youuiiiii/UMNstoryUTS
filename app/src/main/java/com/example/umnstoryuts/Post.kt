package com.example.umnstoryuts

data class Post(
    var id: String = "",
    var uid: String = "",
    var imageUrl: String = "",
    var content: String = "",
    var timestamp: Long = 0L,
    var likes: Int = 0,
    var pinned: Boolean = false,
    var isLiked: Boolean = false
)
