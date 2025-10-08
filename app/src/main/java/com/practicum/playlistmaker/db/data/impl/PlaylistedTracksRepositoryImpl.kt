package com.practicum.playlistmaker.db.data.impl

import com.practicum.playlistmaker.db.data.AppDatabase
import com.practicum.playlistmaker.db.data.converters.PlaylistedTrackDbConvertor
import com.practicum.playlistmaker.db.domain.playlists.PlaylistedTracksRepository
import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import com.practicum.playlistmaker.search.domain.model.Track

class PlaylistedTracksRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistedTrackDbConvertor: PlaylistedTrackDbConvertor
) : PlaylistedTracksRepository {

    override suspend fun insertTrackInPlaylist(playlistedTrack: PlaylistedTrack, track: Track) {
        try {
            appDatabase.playlistedTrackDao()
                .insertTrackInPlaylist(playlistedTrackDbConvertor.map(playlistedTrack, track))
        } catch (e: Exception) {
            println("[INSERT_PLAYLISTED] Error: ${e.message}")
        }
    }

    override suspend fun getPlaylistedTracksIds(playlistId: Int): List<Int> {
        val ids = appDatabase.playlistedTrackDao().getPlaylistedTracksIds(playlistId)
        return ids
    }

    override suspend fun deleteTrackFromPlaylist(trackId: Int, playlistId: Int) {
        try {
            appDatabase.playlistedTrackDao().deleteTrackFromPlaylist(trackId, playlistId)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getTracksByPlaylistId(playlistId: Int): List<Track> {
        try {
            val entities = appDatabase.playlistedTrackDao().getTracksByPlaylistId(playlistId)
            val tracks = entities.map { playlistedTrackDbConvertor.mapToTrack(it)
            }
            return tracks
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getPlaylistDuration(playlistId: Int): Long {
        val duration = appDatabase.playlistedTrackDao().getPlaylistDuration(playlistId) ?: 0L
        return duration
    }
}