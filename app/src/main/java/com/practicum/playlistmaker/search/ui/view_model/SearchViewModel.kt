package com.practicum.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.ui.SearchState
import com.practicum.playlistmaker.search.domain.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>()
    fun getState(): LiveData<SearchState> = _state

    private var latestSearchText: String? = null

    private var searchJob: Job? = null

    fun onSearchClicked() {
        if (!latestSearchText.isNullOrBlank()) searchDebounce(latestSearchText!!)
    }

    fun onSearchQueryChanged(newQuery: String) {
        latestSearchText = newQuery
        if (latestSearchText.isNullOrEmpty()) {
            showHistoryIfAvailable()
            return
        }

        searchDebounce(latestSearchText!!)
    }

    private fun searchRequest(newSearchText: String) {

        if (newSearchText.isNotEmpty()) {
            _state.value = SearchState.Loading

            viewModelScope.launch {
                tracksInteractor
                    .searchTracks(newSearchText)
                    .collect { pair ->
                        processResult(pair.first, pair.second)
                    }
            }
        }
    }

    private fun processResult(foundTracks: List<Track>?, errorMessage: String?) {
        val tracks = mutableListOf<Track>()
        if (foundTracks != null) {
            tracks.addAll(foundTracks)
        }

        when {
            !errorMessage.isNullOrEmpty() ->
                _state.postValue(SearchState.Error(UiMessage.Resource(R.string.internet_error_text)))

            tracks.isEmpty() ->
                _state.postValue(
                    SearchState.EmptyResult(
                        UiMessage.Resource(R.string.not_found_error_text)
                    )
                )

            else -> _state.postValue(SearchState.SearchResult(tracks))
        }
    }

    private fun searchDebounce(changedText: String) {

        latestSearchText = changedText

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchRequest(changedText)
        }
    }

    fun showHistoryIfAvailable() {
        val history = searchHistoryRepository.getSearchHistory()
        if (history.isNotEmpty()) _state.value = SearchState.History(history)
        else _state.value = SearchState.Init
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrackHistory(track)
    }

    fun clearHistory() {
        searchHistoryRepository.clearHistory()
        _state.value = SearchState.Init
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}