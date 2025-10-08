package com.practicum.playlistmaker.db.data.impl

import com.practicum.playlistmaker.db.data.AppDatabase
import com.practicum.playlistmaker.db.data.converters.PlaylistDbConvertor
import com.practicum.playlistmaker.db.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.db.data.entity.PlaylistEntity
import com.practicum.playlistmaker.db.domain.playlists.PlaylistRepository
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConvertor: PlaylistDbConvertor,
    private val trackDbConvertor: TrackDbConvertor
) : PlaylistRepository {
    override suspend fun addPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insertPlaylist(playlistDbConvertor.map(playlist))
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().deletePlaylist(playlistDbConvertor.map(playlist))
    }

    override fun getPlaylistsFlow(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getPlaylistsFlow()
            .map { entities -> convertFromPlaylistEntity(entities) }
    }

    override suspend fun getTracksCount(playlistName: String): Int {
        return appDatabase.playlistDao().getTracksCount(playlistName)
    }

    override suspend fun setTracksCount(
        playlistName: String,
        tracksCount: Int
    ): Int {
        return appDatabase.playlistDao().setTracksCount(playlistName, tracksCount)
    }

    override suspend fun getPlaylistId(playlistName: String): Int {
        return appDatabase.playlistDao().getPlaylistId(playlistName)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().updatePlaylist(playlistDbConvertor.map(playlist))
    }

    override suspend fun getPlaylistById(id: Int): Playlist {
        val playlist = appDatabase.playlistDao().getPlaylistById(id)
            ?: throw IllegalArgumentException("Playlist id $id not found")
        return playlistDbConvertor.map(playlist)
    }

    override suspend fun getTracksByIds(ids: List<Int>): List<Track> {
        return if (ids.isNotEmpty()) {
            appDatabase.trackDao().getTracksByIds(ids).map { trackEntity ->
                trackDbConvertor.map(trackEntity)
            }
        } else {
            emptyList()
        }
    }

    override suspend fun saveTrack(track: Track) {
        try {
            appDatabase.trackDao().insertTrack(trackDbConvertor.map(track))
        } catch (e: Exception) {
            println("[SAVE_TRACK] Error saving track: ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun removeTrackFromPlaylist(
        trackId: Int,
        playlist: Playlist
    ) {
        val updatedPlaylist = playlist.copy(
            tracksCount = playlist.tracksCount - 1,
            tracksIds = playlist.tracksIds.filter { it != trackId }
        )
        updatePlaylist(updatedPlaylist)
        appDatabase.playlistedTrackDao().deleteTrackFromPlaylist(trackId, playlist.id)
    }

    private fun convertFromPlaylistEntity(playlists: List<PlaylistEntity>): List<Playlist> {
        return playlists.map { playlist -> playlistDbConvertor.map(playlist) }
    }
}