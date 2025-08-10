package com.practicum.playlistmaker.db.domain.impl

import com.practicum.playlistmaker.db.domain.FavoriteInteractor
import com.practicum.playlistmaker.db.domain.FavoriteRepository
import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class FavoriteInteractorImpl(
    private val favoriteRepository: FavoriteRepository
) : FavoriteInteractor {
    override suspend fun addTrack(track: Track) {
        favoriteRepository.addTrack(track)
    }

    override suspend fun deleteTrack(track: Track) {
        favoriteRepository.deleteTrack(track)
    }

    override suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteRepository.getFavoriteTracksId().contains(trackId)
    }

    override fun getFavoritesList(): Flow<List<Track>> {
        return favoriteRepository.getFavoriteTracks()
    }
}