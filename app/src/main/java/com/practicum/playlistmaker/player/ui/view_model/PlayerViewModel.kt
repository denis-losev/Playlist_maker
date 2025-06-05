package com.practicum.playlistmaker.player.ui.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.domain.PlayerInteractor

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    init {
        playerInteractor.onPrepared.observeForever { onPrepared() }
        playerInteractor.onCompletion.observeForever { onCompletion() }
    }

    private lateinit var playingTrack: Track

    private val _state = MutableLiveData<PlayerState>()
    fun getState(): LiveData<PlayerState> = _state

    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                _state.postValue(
                    PlayerState.Playing(
                        playingTrack,
                        playerInteractor.getCurrentPosition()
                    )
                )
                handler.postDelayed(this, DEBOUNCE_DELAY)
            }
        }
    }

    fun preparePlayer(track: Track) {
        playingTrack = track
        playerInteractor.prepare(playingTrack)
    }

    private fun onPrepared() {
        _state.postValue(PlayerState.Prepared(playingTrack))
    }

    private fun startPlaying() {
        isPlaying = true
        playerInteractor.play()
        _state.postValue(PlayerState.Playing(playingTrack, playerInteractor.getCurrentPosition()))
        handler.post(updateTimerRunnable)
    }

    private fun pausePlaying() {
        isPlaying = false
        playerInteractor.pause()
        _state.postValue(PlayerState.Paused(playingTrack, playerInteractor.getCurrentPosition()))
        handler.removeCallbacks(updateTimerRunnable)
    }

    fun togglePlayback() {
        if (isPlaying) pausePlaying()
        else startPlaying()
    }

    private fun onCompletion() {
        isPlaying = false
        _state.postValue(PlayerState.Completed(playingTrack))
    }

    override fun onCleared() {
        super.onCleared()
        playerInteractor.release()
        playerInteractor.onPrepared.removeObserver { onPrepared() }
        playerInteractor.onCompletion.removeObserver { onCompletion() }
        handler.removeCallbacks(updateTimerRunnable)
    }

    companion object {
        private const val DEBOUNCE_DELAY = 1000L
        fun getViewModelFactory(playerInteractor: PlayerInteractor): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(playerInteractor)
            }
        }
    }
}