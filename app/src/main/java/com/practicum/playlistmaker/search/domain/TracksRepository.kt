package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.Resource
import com.practicum.playlistmaker.search.domain.model.Track

interface TracksRepository {

    fun searchTracks(searchText: String): Resource<List<Track>>
}