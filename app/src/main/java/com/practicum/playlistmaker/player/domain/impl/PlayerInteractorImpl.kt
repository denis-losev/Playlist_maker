package com.practicum.playlistmaker.player.domain.impl

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.player.data.PlayerRepository
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.search.domain.model.Track

class PlayerInteractorImpl(
    private val playerRepository: PlayerRepository
) : PlayerInteractor {

    override val onPrepared: LiveData<Unit> = playerRepository.onPrepared
    override val onCompletion: LiveData<Unit> = playerRepository.onCompletion

    override fun prepare(track: Track) {
        playerRepository.prepare(track.previewUrl)
    }

    override fun play() {
        playerRepository.play()
    }

    override fun pause() {
        playerRepository.pause()
    }

    override fun release() {
        playerRepository.release()
    }

    override fun getCurrentPosition(): Int {
        return playerRepository.getCurrentPosition()
    }


}