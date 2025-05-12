package com.louiserennick.superpodcast



import androidx.compose.ui.text.LinkAnnotation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louiserennick.superpodcast.model.Episode
import com.louiserennick.superpodcast.model.PodCast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PodcastViewModel : ViewModel() {

    private val _filteredPodcasts = MutableStateFlow<List<PodCast>>(emptyList())
    val filteredPodcasts: StateFlow<List<PodCast>> = _filteredPodcasts

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(PodcastApiService::class.java)
    private val _savedPodcasts = MutableStateFlow < List < PodCast >> (emptyList()) //I create Valuable to save the podcast
    val savedPodcasts: StateFlow <List <PodCast>> = _savedPodcasts

    fun search(term: String, regex: String? = null) { //regex is a optional regural expresion filter that can be use to search podcast title or author
        viewModelScope.launch {
            try {
                val response = api.searchPodcasts(term)
                val podcasts = response.results.map {
                    PodCast(
                        id = it.collectionId.toString(),
                        title = it.collectionName ?: "Unknown Title",
                        author = it.artistName ?: "Unknown Artist",
                        imageUrl = it.artworkUrl100 ?: "",
                        description = "", // Optional
                        feedUrl = it.feedUrl ?: "",
                        podcastUrl = it.collectionViewUrl ?: "" // <-- HERE
                    )
                }.filter { podcast->  //apply filter to the search
                    podcast.title.split(" ").size > 5  &&// Example custom filter
                            (regex.isNullOrEmpty()|| runCatching { //apply regex filter if its available
                                val regexp = Regex (regex, RegexOption.IGNORE_CASE)
                                regexp .containsMatchIn(podcast.title) || regexp.containsMatchIn(podcast.author)
                            }.getOrDefault(false)) //if regex not found dont go by default
                }
                    .sortedBy { it.title .lowercase() } //sorting by title
                _filteredPodcasts.value = podcasts
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


    fun selectPodcast(podcast: PodCast) {
    }
    }
// You can navigate or show details later here