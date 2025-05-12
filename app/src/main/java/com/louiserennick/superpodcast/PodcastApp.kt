package com.louiserennick.superpodcast

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.louiserennick.superpodcast.model.PodCast
import androidx.compose.runtime.collectAsState
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastApp(viewModel: PodcastViewModel) {
    val podcasts by viewModel.filteredPodcasts.collectAsState()
    val savedPodcasts by viewModel.savedPodcasts.collectAsState()

    var searchTerm by remember { mutableStateOf("technology") }
    var regex by remember { mutableStateOf("") }

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

                val context = LocalContext.current

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text("Search results", style = MaterialTheme.typography.headlineSmall)
                    }

                    items(podcasts) { podcast ->
                        PodcastItem(
                            podcast = podcast,
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse(podcast.podcastUrl))
                                context.startActivity(intent)
                            },
                            onSave = { viewModel.savePodcast(it) },
                            isSaved = false
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
        isSaved: Boolean = false
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
        }
    }
