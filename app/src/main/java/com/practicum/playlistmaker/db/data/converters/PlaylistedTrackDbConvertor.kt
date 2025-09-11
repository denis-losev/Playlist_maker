package com.practicum.playlistmaker.db.data.converters

import com.practicum.playlistmaker.db.data.entity.PlaylistedTrackEntity
import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack

class PlaylistedTrackDbConvertor {

    fun map(playlistedTrack: PlaylistedTrack): PlaylistedTrackEntity {
        return PlaylistedTrackEntity(
            trackId = playlistedTrack.trackId,
            playlistId = playlistedTrack.playlistId
        )
    }

    fun map(playlistedTrackEntity: PlaylistedTrackEntity): PlaylistedTrack {
        return PlaylistedTrack(
            trackId = playlistedTrackEntity.trackId,
            playlistId = playlistedTrackEntity.playlistId
        )
    }
}