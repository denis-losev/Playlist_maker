package com.practicum.playlistmaker.db.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.Constants.TRACK_TABLE
import com.practicum.playlistmaker.db.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM $TRACK_TABLE ORDER BY addedAt DESC")
    suspend fun getTracks(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM $TRACK_TABLE")
    suspend fun getTracksId(): List<Int>

    @Query("SELECT * FROM $TRACK_TABLE ORDER BY addedAt DESC")
    fun getTracksFlow(): Flow<List<TrackEntity>>
}