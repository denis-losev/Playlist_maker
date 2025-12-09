package com.practicum.playlistmaker.search.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.practicum.playlistmaker.search.ui.SearchState
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (com.practicum.playlistmaker.search.domain.model.Track) -> Unit
) {
    val state by viewModel.getState().observeAsState(initial = SearchState.Init)
    val context = LocalContext.current

    SearchContent(
        state = state,
        onSearchQueryChanged = { query ->
            viewModel.onSearchQueryChanged(query)
        },
        onSearchClicked = {
            viewModel.onSearchClicked()
        },
        onClearSearch = {
            viewModel.onSearchQueryChanged("")
        },
        onClearHistory = {
            viewModel.clearHistory()
        },
        onRefresh = {
            viewModel.onSearchClicked()
        },
        onTrackClick = { track ->
            viewModel.addTrackToHistory(track)
            onTrackClick(track)
        }
    )
}