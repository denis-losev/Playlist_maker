package com.practicum.playlistmaker.media.ui.composable.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.composable.components.TrackItem
import com.practicum.playlistmaker.media.ui.state.favorites.FavoritesUiState
import com.practicum.playlistmaker.media.ui.state.favorites.toUiState
import com.practicum.playlistmaker.media.ui.view_model.favorites.FavoritesViewModel
import com.practicum.playlistmaker.search.domain.model.Track
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoritesScreen(
    onTrackClick: (Track) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val state by viewModel.getState().observeAsState()

    val uiState = remember(state, context) {
        when (state) {
            null -> FavoritesUiState.Loading
            else -> state!!.toUiState(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fillData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        when (uiState) {
            is FavoritesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
            }

            is FavoritesUiState.Content -> {
                if (uiState.tracks.isEmpty()) {
                    EmptyFavoritesState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(uiState.tracks) { track ->
                            TrackItem(
                                track = track,
                                onClick = {
                                    viewModel.addTrackToHistory(track)
                                    onTrackClick(track)
                                }
                            )
                        }
                    }
                }
            }

            is FavoritesUiState.Empty -> {
                EmptyFavoritesState(
                    emojiRes = uiState.emojiRes,
                    message = uiState.message
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(
    emojiRes: Int = R.drawable.error404emoji,
    message: String = stringResource(R.string.emptyMediaText)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = emojiRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            textAlign = TextAlign.Center,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onBackground
        )
    }
}