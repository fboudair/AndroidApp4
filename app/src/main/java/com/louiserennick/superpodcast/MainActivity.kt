package com.louiserennick.superpodcast


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.louiserennick.superpodcast.ui.theme.SuperPodCastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: PodcastViewModel = viewModel()
            viewModel.search("technology") // Example trigger for now
            SuperPodCastTheme {
                PodcastApp(viewModel)
            }
        }
    }
}


