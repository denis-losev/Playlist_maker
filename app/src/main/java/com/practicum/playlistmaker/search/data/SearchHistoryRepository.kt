package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.domain.model.Track

interface SearchHistoryRepository {

    fun addTrackHistory(track: Track)

    fun getSearchHistory(): ArrayList<Track>

    fun clearHistory()
}