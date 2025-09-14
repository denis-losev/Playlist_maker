package com.practicum.playlistmaker.db.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.practicum.playlistmaker.Constants.PLAYLIST_TABLE

@Entity(tableName = PLAYLIST_TABLE)
class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val coverImage: String,
    val tracksCount: Int = 0,
    val tracksIds: String = ""
)