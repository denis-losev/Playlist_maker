package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.domain.model.Track

interface TracksInteractor {

    fun searchTracks(searchText: String, consumer: TracksConsumer)

    interface TracksConsumer {
        fun consume(recievedTracks: List<Track>?, errorMessage: String?)
    }
}