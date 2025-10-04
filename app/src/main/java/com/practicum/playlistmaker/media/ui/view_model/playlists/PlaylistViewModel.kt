package com.practicum.playlistmaker.media.ui.view_model.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.db.domain.playlists.PlaylistRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistState
import com.practicum.playlistmaker.search.domain.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private lateinit var playlist: Playlist

    private val _state = MutableLiveData<PlaylistState>()
    fun getState(): LiveData<PlaylistState> = _state

    private val _playlistDuration = MutableStateFlow<Long>(0L)
    val playlistDuration: StateFlow<Long> = _playlistDuration

    fun setPlaylist(playlist: Playlist) {
        this.playlist = playlist
        fillData()
    }

    fun fillData() {
        renderState(PlaylistState.Loading)

        viewModelScope.launch {
            try {
                val durationMs = playlistsInteractor.getPlaylistTracksSummDuration(playlist)
                _playlistDuration.value = durationMs
                val tracks = playlistsInteractor.getTracksInPlaylistOptimized(playlist)
                if (tracks.isNotEmpty()) {
                    renderState(PlaylistState.Content(tracks))
                } else {
                    renderState(PlaylistState.Empty(UiMessage.Text("Playlist Empty")))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                renderState(PlaylistState.Empty(UiMessage.Text("Error")))
            }
        }
    }

    suspend fun removeTrackFromPlaylist(trackId: Int, playlist: Playlist): Boolean {
        return playlistsInteractor.removeTrackFromPlaylist(trackId, playlist)
    }

    fun loadPlaylistDuration(playlist: Playlist) {
        viewModelScope.launch {
            try {
                val durationMs = playlistsInteractor.getPlaylistTracksSummDuration(playlist)
                _playlistDuration.value = durationMs
            } catch (e: Exception) {
                println("[VIEWMODEL] Error loading duration: ${e.message}")
            }
        }
    }

    suspend fun getTracksInPlaylist(playlist: Playlist): List<Track> {
        return playlistsInteractor.getTracksInPlaylistOptimized(playlist)
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrackHistory(track)
    }

    suspend fun deletePlaylist(playlist: Playlist)  {
          playlistsInteractor.deletePlaylist(playlist)
    }

    private fun renderState(state: PlaylistState) {
        _state.postValue(state)
    }

    suspend fun updatePlaylist(playlist: Playlist): Boolean {
        return playlistsInteractor.updatePlaylist(playlist)
    }
}