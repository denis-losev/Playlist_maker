package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TracksInteractor {

    fun searchTracks(searchText: String): Flow<Pair<List<Track>?, String?>>

}