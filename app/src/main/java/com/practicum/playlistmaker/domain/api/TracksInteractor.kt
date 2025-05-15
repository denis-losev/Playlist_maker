package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TracksInteractor {

    fun searchTracks(searchText: String, consumer: TracksConsumer)

    interface TracksConsumer {
        fun consume(recievedTracks: List<Track>)
        fun consumeError(error: String?)
    }
}