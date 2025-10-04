package com.practicum.playlistmaker.media.ui.state.playlists

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.model.Track

sealed class PlaylistUiState {

    object Loading : PlaylistUiState()
    data class Content(val tracks: List<Track>) : PlaylistUiState()
    data class Empty(val emojiRes: Int, val message: String) : PlaylistUiState()
}

fun PlaylistState.toUiState(context: Context): PlaylistUiState {
    return when (this) {
        is PlaylistState.Content -> PlaylistUiState.Content(tracks)
        is PlaylistState.Empty -> PlaylistUiState.Empty(
            emojiRes = R.drawable.error404emoji,
            message = message.resolve(context)
        )

        PlaylistState.Loading -> PlaylistUiState.Loading
    }
}