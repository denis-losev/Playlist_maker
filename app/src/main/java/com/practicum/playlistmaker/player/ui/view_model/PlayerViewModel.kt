package com.practicum.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.db.domain.FavoriteInteractor
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteInteractor: FavoriteInteractor
) : ViewModel() {

    init {
        playerInteractor.onPrepared.observeForever { onPrepared() }
        playerInteractor.onCompletion.observeForever { onCompletion() }
    }

    private lateinit var playingTrack: Track

    private val _state = MutableLiveData<PlayerState>()
    fun getState(): LiveData<PlayerState> = _state

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