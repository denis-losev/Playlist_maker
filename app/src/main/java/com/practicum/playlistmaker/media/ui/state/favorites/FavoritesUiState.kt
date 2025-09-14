package com.practicum.playlistmaker.media.ui.state.favorites

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.model.Track

sealed class FavoritesUiState {

    object Loading : FavoritesUiState()
    data class Content(val tracks: List<Track>) : FavoritesUiState()
    data class Empty(val emojiRes: Int, val message: String) : FavoritesUiState()
}

fun FavoritesState.toUiState(context: Context): FavoritesUiState {
    return when (this) {
        is FavoritesState.Content -> FavoritesUiState.Content(tracks)
        is FavoritesState.Empty -> FavoritesUiState.Empty(
            emojiRes = R.drawable.error404emoji,
            message = message.resolve(context)
        )

        FavoritesState.Loading -> FavoritesUiState.Loading
    }
}