package com.practicum.playlistmaker.media.ui.view_model.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.db.domain.favorites.FavoriteInteractor
import com.practicum.playlistmaker.media.ui.state.favorites.FavoritesState
import com.practicum.playlistmaker.search.domain.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteInteractor: FavoriteInteractor,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>()
    fun getState(): LiveData<FavoritesState> = _state

    init {
        fillData()
    }

    fun fillData() {
        renderState(FavoritesState.Loading)
        viewModelScope.launch {
            favoriteInteractor
                .getFavoritesList()
                .collect { tracks ->
                    processResult(tracks)
                }
        }
    }

    private fun processResult(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            renderState(
                FavoritesState.Empty(
                    UiMessage.Resource(R.string.emptyMediaText)
                )
            )
        } else {
            renderState(FavoritesState.Content(tracks))
        }
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrackHistory(track)
    }

    private fun renderState(state: FavoritesState) {
        _state.postValue(state)
    }
}