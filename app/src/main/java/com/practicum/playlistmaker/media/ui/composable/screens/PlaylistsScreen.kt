package com.practicum.playlistmaker.media.ui.composable.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.composable.components.PlaylistItem
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsUiState
import com.practicum.playlistmaker.media.ui.state.playlists.toUiState
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistsScreen(
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (com.practicum.playlistmaker.media.domain.model.Playlist) -> Unit,
    viewModel: PlaylistsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.getState().observeAsState()

    val uiState = remember(state, context) {
        when (state) {
            null -> PlaylistsUiState.Loading
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onCreatePlaylistClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(54.dp)
            ) {
                Text(
                    text = stringResource(R.string.createNewPlaylistText),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            when (uiState) {
                is PlaylistsUiState.Loading -> {
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

                is PlaylistsUiState.Content -> {
                    if (uiState.playlists.isEmpty()) {
                        EmptyPlaylistsState()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(uiState.playlists) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    onClick = { onPlaylistClick(playlist) }
                                )
                            }
                        }
                    }
                }

                is PlaylistsUiState.Empty -> {
                    EmptyPlaylistsState(
                        emojiRes = uiState.emojiRes,
                        message = uiState.message
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPlaylistsState(
    emojiRes: Int = R.drawable.error404emoji,
    message: String = stringResource(R.string.emptyPlaylistsText)
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
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onBackground
        )
    }
}