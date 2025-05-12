package com.louiserennick.superpodcast.model

data class PodCast(
    val id: String,
    val title: String,
    val author: String,
    val imageUrl: String,
    val description: String,
    val feedUrl: String,
    val podcastUrl: String // <-- NEW: iTunes collectionViewUrl
)