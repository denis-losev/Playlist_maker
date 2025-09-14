package com.practicum.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.db.domain.favorites.FavoriteInteractor
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistedTrackState
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsState
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteInteractor: FavoriteInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    init {
        playerInteractor.onPrepared.observeForever { onPrepared() }
        playerInteractor.onCompletion.observeForever { onCompletion() }
    }

    private lateinit var playingTrack: Track

    private val _state = MutableLiveData<PlayerState>()
    fun getState(): LiveData<PlayerState> = _state

    private val _playlistsState = MutableLiveData<PlaylistsState>()
    fun getPlaylistsState(): LiveData<PlaylistsState> = _playlistsState

    private val _playlistedTrackState = MutableLiveData<PlaylistedTrackState>()
    fun getPlaylistedTrackState(): LiveData<PlaylistedTrackState> = _playlistedTrackState

    private var isPlaying = false
    private var progressJob: Job? = null

    fun preparePlayer(track: Track) {
        playingTrack = track
        viewModelScope.launch {
            val isFavorite = favoriteInteractor.isFavorite(track.trackId)
            playingTrack.isFavorite = isFavorite
            playerInteractor.prepare(playingTrack)
        }
    }

    private fun onPrepared() {
        _state.postValue(PlayerState.Prepared(playingTrack))
    }

    private fun startPlaying() {
        isPlaying = true
        playerInteractor.play()
        _state.postValue(PlayerState.Playing(playingTrack, playerInteractor.getCurrentPosition()))
        startProgressUpdater()
    }

    private fun pausePlaying() {
        isPlaying = false
        playerInteractor.pause()
        _state.postValue(PlayerState.Paused(playingTrack, playerInteractor.getCurrentPosition()))
        stopProgressUpdater()
    }

    fun togglePlayback() {
        if (isPlaying) pausePlaying()
        else startPlaying()
    }

    private fun onCompletion() {
        isPlaying = false
        _state.postValue(PlayerState.Completed(playingTrack))
        stopProgressUpdater()
    }

    private fun startProgressUpdater() {
        stopProgressUpdater()
        progressJob = viewModelScope.launch {
            while (isPlaying) {
                delay(DEBOUNCE_DELAY)
                _state.postValue(
                    PlayerState.Playing(
                        playingTrack,
                        playerInteractor.getCurrentPosition()
                    )
                )
            }
        }
    }

    private fun stopProgressUpdater() {
        progressJob?.cancel()
        progressJob = null
    }

    fun toggleFavoriteFlag() {
        val newFav = !playingTrack.isFavorite
        playingTrack.isFavorite = newFav

        _state.postValue(PlayerState.Prepared(playingTrack))

        viewModelScope.launch {
            if (newFav) {
                favoriteInteractor.addTrack(playingTrack)
            } else {
                favoriteInteractor.deleteTrack(playingTrack)
            }
        }
    }

    fun getPlaylists() {
        viewModelScope.launch {
            _playlistsState.postValue(PlaylistsState.Loading)
            try {
                playlistsInteractor
                    .getPlaylists()
                    .collect { playlists ->
                        processPlaylistsResult(playlists)

                    }

            } catch (e: Exception) {
                _playlistsState.postValue(PlaylistsState.Empty(
                    message = UiMessage.Text(e.message.toString())
                ))
            }
        }
    }

    private fun processPlaylistsResult(playlists: List<Playlist>) {
        if (playlists.isNotEmpty()) {
            renderPlaylistsState(PlaylistsState.Content(playlists))
        } else {
            renderPlaylistsState(PlaylistsState.Empty(UiMessage.Text("Empty")))
        }
    }

    private fun renderPlaylistsState(playlistsState: PlaylistsState) {
        _playlistsState.postValue(playlistsState)
    }

    fun tapOnPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            if (playlist.tracksIds.contains(playingTrack.trackId)) {
                _playlistedTrackState.postValue(PlaylistedTrackState.AlreadyExists(playlist.name))
                return@launch
            }

            try {
                val success = playlistsInteractor.addTrackToPlaylist(playingTrack, playlist)
                if (success) {
                    _playlistedTrackState.postValue(PlaylistedTrackState.Success(playlist.name))
                    getPlaylists()
                } else {
                    _playlistedTrackState.postValue(PlaylistedTrackState.Error("Failed to add track"))
                }
            } catch (e: Exception) {
                _playlistedTrackState.postValue(PlaylistedTrackState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerInteractor.release()
        playerInteractor.onPrepared.removeObserver { onPrepared() }
        playerInteractor.onCompletion.removeObserver { onCompletion() }
        stopProgressUpdater()
    }

    companion object {
        private const val DEBOUNCE_DELAY = 300L
    }
}