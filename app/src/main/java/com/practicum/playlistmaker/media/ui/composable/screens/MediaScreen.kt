package com.practicum.playlistmaker.media.ui.composable.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.view_model.favorites.FavoritesViewModel
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistsViewModel
import com.practicum.playlistmaker.search.domain.model.Track

@Composable
fun MediaScreen(
    onTrackClick: (Track) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    playlistsViewModel: PlaylistsViewModel
) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.favoriteTracks),
        stringResource(R.string.playlists)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedTabIndex == index) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> FavoritesScreen(
                    onTrackClick = onTrackClick,
                    viewModel = favoritesViewModel
                )
                1 -> PlaylistsScreen(
                    onCreatePlaylistClick = onCreatePlaylistClick,
                    onPlaylistClick = onPlaylistClick,
                    viewModel = playlistsViewModel
                )
            }
        }
    }
}