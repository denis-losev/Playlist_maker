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
import com.practicum.playlistmaker.player.service.MusicService
import com.practicum.playlistmaker.player.service.PlaybackState
import com.practicum.playlistmaker.utils.UiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class PlayerViewModel(
    private val favoriteInteractor: FavoriteInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var playingTrack: Track? = null
    private var musicService: MusicService? = null
    private var isBound = false

    private var progressJob: Job? = null

    private val _state = MutableLiveData<PlayerState>()
    fun getState(): LiveData<PlayerState> = _state

    private val _playlistsState = MutableLiveData<PlaylistsState>()
    fun getPlaylistsState(): LiveData<PlaylistsState> = _playlistsState

    private val _playlistedTrackState = MutableLiveData<PlaylistedTrackState>()
    fun getPlaylistedTrackState(): LiveData<PlaylistedTrackState> = _playlistedTrackState

    fun setMusicService(service: MusicService?) {
        musicService = service
        isBound = service != null

        if (service != null) {
            observeServiceState()
        }
    }

    private fun observeServiceState() {
        musicService?.playbackState?.observeForever { state ->
            val currentTrack = playingTrack ?: return@observeForever

            when (state) {
                PlaybackState.PREPARED -> {
                    _state.postValue(PlayerState.Prepared(currentTrack))
                }
                PlaybackState.PLAYING -> {
                    startProgressUpdater()
                    _state.postValue(PlayerState.Playing(currentTrack, musicService?.getCurrentPosition() ?: 0))
                }
                PlaybackState.PAUSED -> {
                    stopProgressUpdater()
                    _state.postValue(PlayerState.Paused(currentTrack, musicService?.getCurrentPosition() ?: 0))
                }
                PlaybackState.COMPLETED -> {
                    stopProgressUpdater()
                    _state.postValue(PlayerState.Completed(currentTrack))
                    musicService?.hideNotification()
                }
                PlaybackState.IDLE -> {
                    _state.postValue(PlayerState.Default(currentTrack))
                }
                else -> {}
            }
        }
    }

    fun preparePlayer(track: Track) {
        playingTrack = track
        viewModelScope.launch {
            val isFavorite = favoriteInteractor.isFavorite(track.trackId)
            playingTrack?.isFavorite = isFavorite
            _state.postValue(PlayerState.Prepared(track))
        }
    }

    fun togglePlayback() {
        when (musicService?.playbackState?.value) {
            PlaybackState.PLAYING -> musicService?.pause()
            PlaybackState.PAUSED, PlaybackState.PREPARED -> musicService?.play()
            else -> {}
        }
    }

    private fun startProgressUpdater() {
        stopProgressUpdater()
        progressJob = viewModelScope.launch {
            while (musicService?.playbackState?.value == PlaybackState.PLAYING) {
                delay(DEBOUNCE_DELAY)
                val position = musicService?.getCurrentPosition() ?: 0
                val currentTrack = playingTrack ?: break
                _state.postValue(PlayerState.Playing(currentTrack, position))
            }
        }
    }

    private fun stopProgressUpdater() {
        progressJob?.cancel()
        progressJob = null
    }

    fun onAppMinimized() {
        if (isBound && musicService?.playbackState?.value == PlaybackState.PLAYING) {
            musicService?.showNotification()
        }
    }

    fun onAppResumed() {
        if (isBound) {
            musicService?.hideNotification()
        }
    }

    fun onScreenClosed() {
        if (isBound) {
            musicService?.pause()
            musicService?.hideNotification()
        }
    }

    fun toggleFavoriteFlag() {
        val currentTrack = playingTrack ?: return
        val newFav = !currentTrack.isFavorite
        currentTrack.isFavorite = newFav

        _state.postValue(PlayerState.Prepared(currentTrack))

        viewModelScope.launch {
            if (newFav) {
                favoriteInteractor.addTrack(currentTrack)
            } else {
                favoriteInteractor.deleteTrack(currentTrack)
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
        val currentTrack = playingTrack ?: return

        viewModelScope.launch {
            if (playlist.tracksIds.contains(currentTrack.trackId)) {
                _playlistedTrackState.postValue(PlaylistedTrackState.AlreadyExists(playlist.name))
                return@launch
            }

            try {
                val success = playlistsInteractor.addTrackToPlaylist(currentTrack, playlist)
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
        stopProgressUpdater()
    }

    companion object {
        private const val DEBOUNCE_DELAY = 300L
    }
}