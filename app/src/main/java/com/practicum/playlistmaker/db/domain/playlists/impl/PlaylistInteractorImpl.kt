package com.practicum.playlistmaker.db.domain.playlists.impl

import com.practicum.playlistmaker.db.domain.playlists.PlaylistRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistedTracksRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository,
    private val playlistedTracksRepository: PlaylistedTracksRepository
) :
    PlaylistsInteractor {
    override suspend fun addPlaylist(playlist: Playlist) {
        playlistRepository.addPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistRepository.deletePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getPlaylistsFlow()
    }

    override suspend fun getTracksCount(playlistName: String): Int {
        return playlistRepository.getTracksCount(playlistName)
    }

    override suspend fun setTracksCount(
        playlistName: String,
        tracksCount: Int
    ): Int {
        return playlistRepository.setTracksCount(playlistName, tracksCount)
    }

    override suspend fun getPlaylistId(playlistName: String): Int {
        return playlistRepository.getPlaylistId(playlistName)
    }

    override suspend fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ): Boolean {
        return try {

            val updatedPlaylist = playlist.copy(
                tracksCount = playlist.tracksCount + 1,
                tracksIds = playlist.tracksIds + track.trackId
            )
            playlistRepository.updatePlaylist(updatedPlaylist)
            val playlistedTrack = PlaylistedTrack(
                trackId = track.trackId,
                playlistId = playlist.id
            )
            playlistedTracksRepository.insertTrackInPlaylist(playlistedTrack, track)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getPlaylistTracksSummDuration(playlist: Playlist): Long {
        return playlistedTracksRepository.getPlaylistDuration(playlist.id)
    }

    override suspend fun getTracksInPlaylist(playlist: Playlist): List<Track> {
        return getTracksInPlaylistOptimized(playlist)
    }

    override suspend fun getTracksInPlaylistOptimized(playlist: Playlist): List<Track> {
        return playlistedTracksRepository.getTracksByPlaylistId(playlist.id)
    }

    override suspend fun removeTrackFromPlaylist(
        trackId: Int,
        playlist: Playlist
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                playlistedTracksRepository.deleteTrackFromPlaylist(trackId, playlist.id)

                val updatedPlaylist = playlist.copy(
                    tracksCount = playlist.tracksCount - 1,
                    tracksIds = playlist.tracksIds.filter { it != trackId }
                )
                playlistRepository.updatePlaylist(updatedPlaylist)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist): Boolean {
        return try {
            playlistRepository.updatePlaylist(playlist)
            true
        } catch (e: Exception) {
            false
        }
    }
}