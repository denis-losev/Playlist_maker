package com.practicum.playlistmaker.db.domain.playlists

import com.practicum.playlistmaker.media.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    suspend fun addPlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
    fun getPlaylistsFlow(): Flow<List<Playlist>>
    suspend fun getTracksCount(playlistName: String): Int
    suspend fun setTracksCount(playlistName: String, tracksCount: Int): Int
    suspend fun getPlaylistId(playlistName: String): Int
    suspend fun updatePlaylist(playlist: Playlist)
}