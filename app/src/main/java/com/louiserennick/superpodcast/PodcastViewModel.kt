
package com.louiserennick.superpodcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louiserennick.superpodcast.model.PodCast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class Filter {  //creating enum class for different filters
    ascendingTitle, descendingTitle, ascendingAuthor, descendingAuthor
}

class PodcastViewModel : ViewModel() {

    private val _filteredPodcasts = MutableStateFlow<List<PodCast>>(emptyList())
    val filteredPodcasts: StateFlow<List<PodCast>> = _filteredPodcasts

    private val _favourite = MutableStateFlow<List<PodCast>>(emptyList())
    val favourite: StateFlow<List<PodCast>> = _favourite //creating a variable to keep track up favourite podcasr

    var curfilter = Filter.ascendingTitle

    private val _savedPodcasts = MutableStateFlow<List<PodCast>>(emptyList())
    val savedPodcasts: StateFlow<List<PodCast>> = _savedPodcasts

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(PodcastApiService::class.java)

    fun addToFavourite(podcast: PodCast) { // a function to add a podcast to favourite list
        if (_favourite.value.none { it.id == podcast.id }) {
            _favourite.value = _favourite.value + podcast
        }
    }

    fun removeFavourite(podcast: PodCast) { //a function to remove a podcast from favourite list
        _favourite.value = _favourite.value.filterNot { it.id == podcast.id }
    }

    fun search(term: String, regex: String? = null) {
        viewModelScope.launch {
            try {
                val response = api.searchPodcasts(term)
                val filtered = response.results.map {
                    PodCast(
                        id = it.collectionId.toString(),
                        title = it.collectionName ?: "Unknown Title",
                        author = it.artistName ?: "Unknown Artist",
                        imageUrl = it.artworkUrl100 ?: "",
                        description = "",
                        feedUrl = it.feedUrl ?: "",
                        podcastUrl = it.collectionViewUrl ?: ""
                    )
                }.filter { podcast ->
                    podcast.title.split(" ").size > 5 &&
                            (regex.isNullOrEmpty() || runCatching {
                                val regexp = Regex(regex, RegexOption.IGNORE_CASE)
                                regexp.containsMatchIn(podcast.title) || regexp.containsMatchIn(podcast.author)
                            }.getOrDefault(false))
                }

                val sorted = when (curfilter) { //sorting the results of the search base on different filter
                    Filter.ascendingTitle -> filtered.sortedBy { it.title.lowercase() }
                    Filter.descendingTitle -> filtered.sortedByDescending { it.title.lowercase() }
                    Filter.ascendingAuthor -> filtered.sortedBy { it.author.lowercase() }
                    Filter.descendingAuthor -> filtered.sortedByDescending { it.author.lowercase() }
                }

                _filteredPodcasts.value = sorted
            } catch (e: Exception) {
                e.printStackTrace()
                _filteredPodcasts.value = emptyList()
            }
        }
    }

    fun savePodcast(podcast: PodCast) {
        if (_savedPodcasts.value.none { it.id == podcast.id }) {
            _savedPodcasts.value = _savedPodcasts.value + podcast
        }
    }

    fun removesave(podcast: PodCast) {
        _savedPodcasts.value = _savedPodcasts.value.filterNot { it.id == podcast.id }
    }

    fun selectPodcast(podcast: PodCast) {}
}