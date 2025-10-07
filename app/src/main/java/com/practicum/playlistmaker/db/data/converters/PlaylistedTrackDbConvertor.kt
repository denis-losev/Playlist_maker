package com.practicum.playlistmaker.db.data.converters

import com.practicum.playlistmaker.db.data.entity.PlaylistedTrackEntity
import com.practicum.playlistmaker.media.domain.model.PlaylistedTrack
import com.practicum.playlistmaker.search.domain.model.Track

class PlaylistedTrackDbConvertor {

    fun map(playlistedTrack: PlaylistedTrack, track: Track): PlaylistedTrackEntity {
        return PlaylistedTrackEntity(
            trackId = playlistedTrack.trackId,
            playlistId = playlistedTrack.playlistId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            addedAt = System.currentTimeMillis()
        )
    }

    fun map(playlistedTrackEntity: PlaylistedTrackEntity): PlaylistedTrack {
        return PlaylistedTrack(
            trackId = playlistedTrackEntity.trackId,
            playlistId = playlistedTrackEntity.playlistId
        )
    }

    fun mapToTrack(playlistedTrackEntity: PlaylistedTrackEntity): Track {
        return Track(
            trackId = playlistedTrackEntity.trackId,
            trackName = playlistedTrackEntity.trackName,
            artistName = playlistedTrackEntity.artistName,
            trackTimeMillis = playlistedTrackEntity.trackTimeMillis,
            artworkUrl100 = playlistedTrackEntity.artworkUrl100,
            collectionName = playlistedTrackEntity.collectionName,
            releaseDate = playlistedTrackEntity.releaseDate,
            primaryGenreName = playlistedTrackEntity.primaryGenreName,
            country = playlistedTrackEntity.country,
            previewUrl = playlistedTrackEntity.previewUrl
        )
    }
}