package com.practicum.playlistmaker.player.data.impl

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.player.domain.PlayerRepository

class PlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayer
) : PlayerRepository {

    private val _onPrepared = MutableLiveData<Unit>()
    override val onPrepared: LiveData<Unit> = _onPrepared

    private val _onCompletion = MutableLiveData<Unit>()
    override val onCompletion: LiveData<Unit> = _onCompletion


    override fun prepare(previewUrl: String?) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.setOnPreparedListener { _onPrepared.postValue(Unit) }
        mediaPlayer.setOnCompletionListener { _onCompletion.postValue(Unit) }
        mediaPlayer.prepareAsync()
    }

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}