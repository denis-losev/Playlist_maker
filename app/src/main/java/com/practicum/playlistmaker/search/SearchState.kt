package com.practicum.playlistmaker.search

import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.search.ui.UiMessage

sealed interface SearchState {
    object Init: SearchState
    data class History(val tracks: List<Track>): SearchState
    object Loading : SearchState
    data class SearchResult(val tracks: List<Track>) : SearchState
    data class EmptyResult(val message: UiMessage) : SearchState
    data class Error(val errorMessage: UiMessage): SearchState
}