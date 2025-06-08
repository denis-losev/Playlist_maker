package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.Resource
import com.practicum.playlistmaker.search.domain.SearchDebouncer
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.domain.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()
    private val debouncer = SearchDebouncer()

    override fun searchTracks(searchText: String, consumer: TracksInteractor.TracksConsumer) {
        debouncer.submit {
            executor.execute {
                when (val resource = repository.searchTracks(searchText)) {
                    is Resource.Error<*> -> consumer.consume(null, resource.message)
                    is Resource.Success<*> -> consumer.consume(resource.data, null)
                }
            }
        }
    }
}