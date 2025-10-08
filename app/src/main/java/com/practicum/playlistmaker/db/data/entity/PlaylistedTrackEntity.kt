package com.practicum.playlistmaker.db.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.practicum.playlistmaker.Constants.PLAYLISTED_TRACKS_TABLE

@Entity(tableName = PLAYLISTED_TRACKS_TABLE)
data class PlaylistedTrackEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var trackId: Int,
    var playlistId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
)
