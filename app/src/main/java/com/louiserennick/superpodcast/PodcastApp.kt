package com.louiserennick.superpodcast

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.louiserennick.superpodcast.model.PodCast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastApp(viewModel: PodcastViewModel) {
    val podcasts by viewModel.filteredPodcasts.collectAsState()
    val savedPodcasts by viewModel.savedPodcasts.collectAsState()
    val favourite by viewModel.favourite.collectAsState()

    var searchTerm by remember { mutableStateOf("technology") }
    var regex by remember { mutableStateOf("") }
    var sortLabel by remember { mutableStateOf("Sort By Title A-Z") } //variable to hold label of the sort button

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SuperPodcast") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(paddingValues).padding(8.dp)) {
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    label = { Text("Search podcasts") },
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { searchTerm = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = regex,
                    onValueChange = { regex = it },
                    label = { Text("Regex filter") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.search(searchTerm, regex) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Search")
                }
                Button(   //creating a new button for sort
                    onClick = { //when the sort button is clicked the next sort option will appear
                        viewModel.curfilter = when (viewModel.curfilter) {
                            Filter.descendingTitle -> {
                                sortLabel = "Sort By Author A-Z"
                                Filter.ascendingAuthor
                            }
                            Filter.ascendingAuthor -> {
                                sortLabel = "Sort By Author Z-A"
                                Filter.descendingAuthor
                            }
                            Filter.descendingAuthor -> {
                                sortLabel = "Sort By Title A-Z"
                                Filter.ascendingTitle
                            }
                            Filter.ascendingTitle -> {
                                sortLabel = "Sort By Title Z-A"
                                Filter.descendingTitle
                            }
                        }
                        viewModel.search(searchTerm, regex)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(sortLabel)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text("Search results", style = MaterialTheme.typography.headlineSmall)
                    }

                    items(podcasts) { podcast ->
                        val isfavourite = favourite.any { it.id == podcast.id }
                        PodcastItem(
                            podcast = podcast,
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse(podcast.podcastUrl))
                                context.startActivity(intent)
                            },
                            onSave = { viewModel.savePodcast(it) },
                            isSaved = savedPodcasts.any { it.id == podcast.id },
                            isfavourite = isfavourite,
                            onfavourite = {
                                if (isfavourite) viewModel.removeFavourite(it)
                                else viewModel.addToFavourite(it)
                            }
                        )
                    }

                    if (savedPodcasts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Saved for Later", style = MaterialTheme.typography.headlineSmall)
                        }

                        items(savedPodcasts) { saved ->
                            PodcastItem(
                                podcast = saved,
                                onClick = {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(saved.podcastUrl))
                                    context.startActivity(intent)
                                },
                                onSave = { viewModel.removesave(it) },
                                isSaved = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodcastItem(
    podcast: PodCast,
    onClick: (PodCast) -> Unit,
    onSave: (PodCast) -> Unit,
    isSaved: Boolean = false,
    isfavourite: Boolean = false,
    onfavourite: (PodCast) -> Unit = {}
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(podcast) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(podcast.imageUrl),
            contentDescription = podcast.title,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = podcast.title, style = MaterialTheme.typography.titleMedium)
            Text(text = podcast.author, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = {
                onSave(podcast)
                val message = if (isSaved) "Removed from saved list" else "Added to saved list"
                Toast.makeText(context, "$message: ${podcast.title}", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(if (isSaved) "Remove" else "Save")
        }
        IconButton(onClick = { onfavourite(podcast) }) {
            Icon(
                imageVector = if (isfavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favourite"
            )
        }
    }
}