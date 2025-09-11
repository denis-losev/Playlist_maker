package com.practicum.playlistmaker.db.data.impl

import com.practicum.playlistmaker.db.data.AppDatabase
import com.practicum.playlistmaker.db.data.converters.PlaylistedTrackDbConvertor
import com.practicum.playlistmaker.db.domain.playlists.PlaylistedTracksRepository
import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import kotlinx.coroutines.flow.Flow

class PlaylistedTracksRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistedTrackDbConvertor: PlaylistedTrackDbConvertor
) : PlaylistedTracksRepository {
    override suspend fun insertTrackInPlaylist(playlistedTrack: PlaylistedTrack) {
        appDatabase.playlistedTrackDao().insertTrackInPlaylist(playlistedTrackDbConvertor.map(playlistedTrack))
    }

    override fun getPlaylistedTracksIdFlow(playlistId: Int): Flow<List<Int>> {
        return appDatabase.playlistedTrackDao().getPlaylistedTracksIdFlow(playlistId)
    }
}