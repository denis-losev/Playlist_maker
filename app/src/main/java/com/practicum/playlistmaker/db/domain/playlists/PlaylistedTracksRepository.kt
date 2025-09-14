package com.practicum.playlistmaker.db.domain.playlists

import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import kotlinx.coroutines.flow.Flow

interface PlaylistedTracksRepository {

    suspend fun insertTrackInPlaylist(playlistedTrack: PlaylistedTrack)
    fun getPlaylistedTracksIdFlow(playlistId: Int): Flow<List<Int>>
}