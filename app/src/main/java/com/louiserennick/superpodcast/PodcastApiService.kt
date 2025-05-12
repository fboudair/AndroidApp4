package com.louiserennick.superpodcast


import retrofit2.http.GET
import retrofit2.http.Query

// Individual podcast result
data class ITunesPodcast(
    val collectionId: Long,
    val collectionName: String,
    val artistName: String,
    val artworkUrl100: String,
    val feedUrl: String,
    val collectionViewUrl: String?
)

// Search API response
data class ITunesResponse(
    val results: List<ITunesPodcast>
)

interface PodcastApiService {
    @GET("search?media=podcast")
    suspend fun searchPodcasts(@Query("term") term: String): ITunesResponse
}
