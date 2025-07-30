package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.Resource
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.domain.TracksRepository
import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(searchText: String): Flow<Pair<List<Track>?, String?>> {
        return repository.searchTracks(searchText).map { result ->
            when (result) {
                is Resource.Error<*> -> Pair(null, result.message)
                is Resource.Success<*> -> Pair(result.data, null)
            }
        }
    }
}