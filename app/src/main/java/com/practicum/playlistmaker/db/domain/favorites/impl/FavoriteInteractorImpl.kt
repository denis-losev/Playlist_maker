package com.practicum.playlistmaker.db.domain.favorites.impl

import com.practicum.playlistmaker.db.domain.favorites.FavoriteInteractor
import com.practicum.playlistmaker.db.domain.favorites.FavoriteRepository
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