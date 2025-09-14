package com.practicum.playlistmaker.media.ui.view_model.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.db.domain.playlists.PlaylistRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsState
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val playlistRepository: PlaylistRepository
): ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>()
    fun getState(): LiveData<PlaylistsState> = _state

    init {
        fillData()
    }

    fun fillData() {
        renderState(PlaylistsState.Loading)
        viewModelScope.launch {
            playlistsInteractor
                .getPlaylists()
                .collect { playlists ->
                    processResult(playlists)
                }
        }
    }

    private fun processResult(playlists: List<Playlist>) {
        if (playlists.isEmpty()) {
            renderState(
                PlaylistsState.Empty(
                    UiMessage.Resource(R.string.emptyPlaylistsText)
                )
            )
        } else {
            renderState(PlaylistsState.Content(playlists))
        }
    }



    private fun renderState(state: PlaylistsState) {
        _state.postValue(state)
    }
}