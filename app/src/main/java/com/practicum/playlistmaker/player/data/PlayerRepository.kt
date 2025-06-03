package com.practicum.playlistmaker.player.data

import androidx.lifecycle.LiveData

interface PlayerRepository {
    val onPrepared: LiveData<Unit>
    val onCompletion: LiveData<Unit>

    fun prepare(previewUrl: String?)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Int
}