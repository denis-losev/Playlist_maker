package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryRepository {

    fun addTrackHistory(track: Track)

    fun getSearchHistory(): ArrayList<Track>

    fun clearHistory()
}