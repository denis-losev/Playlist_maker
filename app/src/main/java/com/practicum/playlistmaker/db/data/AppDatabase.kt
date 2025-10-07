package com.practicum.playlistmaker.db.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.db.data.dao.PlaylistDao
import com.practicum.playlistmaker.db.data.dao.PlaylistedTrackDao
import com.practicum.playlistmaker.db.data.dao.TrackDao
import com.practicum.playlistmaker.db.data.entity.PlaylistEntity
import com.practicum.playlistmaker.db.data.entity.PlaylistedTrackEntity
import com.practicum.playlistmaker.db.data.entity.TrackEntity

@Database(
    version = 5,
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistedTrackEntity::class]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistedTrackDao(): PlaylistedTrackDao
}