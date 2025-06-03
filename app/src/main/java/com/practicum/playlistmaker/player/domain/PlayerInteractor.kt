package com.practicum.playlistmaker.player.domain

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.search.domain.model.Track

interface PlayerInteractor {
    val onPrepared: LiveData<Unit>
    val onCompletion: LiveData<Unit>

    fun prepare(track: Track)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Int
}