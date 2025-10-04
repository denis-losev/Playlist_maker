package com.practicum.playlistmaker.db.domain.playlists

import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import com.practicum.playlistmaker.search.domain.model.Track

interface PlaylistedTracksRepository {

    suspend fun insertTrackInPlaylist(playlistedTrack: PlaylistedTrack)
    suspend fun getPlaylistedTracksSummDuration(ids: List<Int>): Long
    suspend fun getPlaylistedTracksIds(playlistId: Int): List<Int>
    suspend fun getTracksByIds(ids: List<Int>): List<Track>

    suspend fun getTracksByPlaylistId(playlistId: Int): List<Track>
    suspend fun getPlaylistDuration(playlistId: Int): Long
}