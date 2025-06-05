package com.practicum.playlistmaker.player

import com.practicum.playlistmaker.search.domain.model.Track


sealed interface PlayerState {
    data class Default(val track: Track) : PlayerState
    data class Prepared(val track: Track) : PlayerState
    data class Playing(val track: Track, val currentPosition: Int) : PlayerState
    data class Paused(val track: Track, val currentPosition: Int) : PlayerState
    data class Completed(val track: Track) : PlayerState
}