package com.practicum.playlistmaker.db.domain

import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteInteractor {

    suspend fun addTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    suspend fun isFavorite(trackId: Int): Boolean
    fun getFavoritesList(): Flow<List<Track>>
}