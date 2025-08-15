package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage

sealed class SearchState {
    object Init : SearchState()
    object Loading : SearchState()

    data class Error(val errorMessage: UiMessage) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    data class SearchResult(val tracks: List<Track>) : SearchState()
    data class EmptyResult(val message: UiMessage) : SearchState()
}