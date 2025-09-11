package com.practicum.playlistmaker.db.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.practicum.playlistmaker.Constants.PLAYLISTED_TRACKS_TABLE

@Entity(tableName = PLAYLISTED_TRACKS_TABLE)
data class PlaylistedTrackEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var trackId: Int,
    var playlistId: Int
)
