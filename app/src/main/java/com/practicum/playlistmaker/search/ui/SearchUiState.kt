package com.practicum.playlistmaker.search.ui

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.model.Track

sealed class SearchUiState {
    object Init : SearchUiState()
    object Loading : SearchUiState()
    data class ShowError(val emojiRes: Int, val message: String, val showRefresh: Boolean = false) :
        SearchUiState()

    data class ShowHistory(val tracks: List<Track>) : SearchUiState()
    data class ShowTracks(val tracks: List<Track>) : SearchUiState()
}

fun SearchState.toUiState(context: Context): SearchUiState {
    return when (this) {
        SearchState.Init -> SearchUiState.Init
        SearchState.Loading -> SearchUiState.Loading
        is SearchState.EmptyResult -> SearchUiState.ShowError(
            emojiRes = R.drawable.error404emoji,
            message = message.resolve(context),
            showRefresh = false
        )

        is SearchState.Error -> SearchUiState.ShowError(
            emojiRes = R.drawable.interneterroremoji,
            message = errorMessage.resolve(context),
            showRefresh = true
        )

        is SearchState.History -> SearchUiState.ShowHistory(tracks)
        is SearchState.SearchResult -> SearchUiState.ShowTracks(tracks)
    }
}