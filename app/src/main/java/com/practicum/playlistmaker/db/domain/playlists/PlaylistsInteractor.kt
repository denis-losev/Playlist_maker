package com.practicum.playlistmaker.db.domain.playlists

import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {

    suspend fun addPlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getTracksCount(playlistName: String): Int
    suspend fun setTracksCount(playlistName: String, tracksCount: Int): Int
    suspend fun getPlaylistId(playlistName: String): Int
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean
}