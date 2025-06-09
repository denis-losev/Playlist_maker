package com.practicum.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.SearchState
import com.practicum.playlistmaker.search.ui.UiMessage
import com.practicum.playlistmaker.search.domain.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.domain.model.Track

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>()
    fun getState(): LiveData<SearchState> = _state

    private var searchText = ""

    fun onSearchClicked() {
        if (searchText.isNotBlank()) searchRequest()
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchText = newQuery
        if (searchText.isEmpty()) {
            showHistoryIfAvailable()
            return
        }

        searchRequest()
    }

    private fun searchRequest() {
        _state.value = SearchState.Loading
        tracksInteractor.searchTracks(searchText, object : TracksInteractor.TracksConsumer {
            override fun consume(recievedTracks: List<Track>?, errorMessage: String?) {
                when {
                    !errorMessage.isNullOrEmpty() -> _state.postValue(SearchState.Error(UiMessage.Resource(R.string.internet_error_text)))
                    recievedTracks.isNullOrEmpty() -> _state.postValue(
                        SearchState.EmptyResult(
                        UiMessage.Resource(R.string.not_found_error_text)))
                    else -> _state.postValue(SearchState.SearchResult(recievedTracks))
                }
            }
        })
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
}