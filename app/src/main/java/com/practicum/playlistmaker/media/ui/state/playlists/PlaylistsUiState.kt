package com.practicum.playlistmaker.media.ui.state.playlists

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.model.Playlist

sealed class PlaylistsUiState {

    object Loading : PlaylistsUiState()
    data class Content(val playlists: List<Playlist>) : PlaylistsUiState()
    data class Empty(val emojiRes: Int, val message: String) : PlaylistsUiState()
}

fun PlaylistsState.toUiState(context: Context): PlaylistsUiState {
    return when (this) {
        is PlaylistsState.Content -> PlaylistsUiState.Content(playlists)
        is PlaylistsState.Empty -> PlaylistsUiState.Empty(
            emojiRes = R.drawable.error404emoji,
            message = message.resolve(context)
        )

        PlaylistsState.Loading -> PlaylistsUiState.Loading
    }
}