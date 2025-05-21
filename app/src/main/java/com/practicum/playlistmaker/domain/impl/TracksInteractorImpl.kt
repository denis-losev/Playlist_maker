package com.practicum.playlistmaker.domain.impl

import SearchDebouncer
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()
    private val debouncer = SearchDebouncer()

    override fun searchTracks(searchText: String, consumer: TracksInteractor.TracksConsumer) {
        debouncer.submit {
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
}