package com.practicum.playlistmaker.db.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.Constants.PLAYLISTED_TRACKS_TABLE
import com.practicum.playlistmaker.Constants.TRACK_TABLE
import com.practicum.playlistmaker.db.data.entity.PlaylistedTrackEntity
import com.practicum.playlistmaker.db.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistedTrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackInPlaylist(playlistedTrackEntity: PlaylistedTrackEntity)

    @Query("""
    SELECT trackId FROM $PLAYLISTED_TRACKS_TABLE 
    WHERE playlistId = :playlistId
    """)
    fun getPlaylistedTracksIdFlow(playlistId: Int): Flow<List<Int>>

    @Query("""
    SELECT trackId FROM $PLAYLISTED_TRACKS_TABLE 
    WHERE playlistId = :playlistId
    """)
    suspend fun getPlaylistedTracksIds(playlistId: Int): List<Int>

    @Query("""
    SELECT t.* FROM $TRACK_TABLE t 
    INNER JOIN $PLAYLISTED_TRACKS_TABLE pt ON t.trackId = pt.trackId 
    WHERE pt.playlistId = :playlistId ORDER BY pt.id DESC
    """)
    suspend fun getTracksByPlaylistId(playlistId: Int): List<TrackEntity>

    @Query("""
    SELECT SUM(t.trackTimeMillis) FROM $TRACK_TABLE t
    INNER JOIN $PLAYLISTED_TRACKS_TABLE pt ON t.trackId = pt.trackId
    WHERE pt.playlistId = :playlistId
    """)
    suspend fun getPlaylistDuration(playlistId: Int): Long

    @Query("""
    DELETE FROM $PLAYLISTED_TRACKS_TABLE 
    WHERE trackId = :trackId AND playlistId = :playlistId
    """)
    fun deleteTrackFromPlaylist(trackId: Int, playlistId: Int)
}