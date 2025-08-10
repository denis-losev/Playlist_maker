package com.practicum.playlistmaker.media.ui

import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage

sealed class FavoritesState {

    object Loading : FavoritesState()
    data class Content(val tracks: List<Track>) : FavoritesState()
    data class Empty(val message: UiMessage) : FavoritesState()
}

