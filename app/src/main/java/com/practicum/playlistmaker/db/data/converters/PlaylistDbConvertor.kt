package com.practicum.playlistmaker.db.data.converters

import com.practicum.playlistmaker.db.data.entity.PlaylistEntity
import com.practicum.playlistmaker.media.domain.model.Playlist

class PlaylistDbConvertor {

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            coverImage = playlist.image,
            tracksCount = playlist.tracksCount,
            description = playlist.description,
            tracksIds = playlist.tracksIds.joinToString(",")
        )
    }

    fun map(playlistEntity: PlaylistEntity): Playlist {
        return Playlist(
            name = playlistEntity.name,
            image = playlistEntity.coverImage,
            tracksCount = playlistEntity.tracksCount,
            id = playlistEntity.id,
            description = playlistEntity.description,
            tracksIds = if (playlistEntity.tracksIds.isNotEmpty()) {
                playlistEntity.tracksIds.split(",").mapNotNull { it.toIntOrNull() }
            } else {
                emptyList()
            }
        )
    }
}