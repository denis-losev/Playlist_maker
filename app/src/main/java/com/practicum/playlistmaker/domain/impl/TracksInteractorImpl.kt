package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(searchText: String, consumer: TracksInteractor.TracksConsumer) {

        executor.execute {
            try {
                val tracksResponse = repository.searchTracks(searchText)
                consumer.consume(tracksResponse)
            } catch (e: Exception) {
                consumer.consumeError(e.localizedMessage)
            }
        }
    }
}