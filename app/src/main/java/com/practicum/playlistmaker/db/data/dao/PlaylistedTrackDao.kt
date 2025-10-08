package com.practicum.playlistmaker.db.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.Constants.PLAYLISTED_TRACKS_TABLE
import com.practicum.playlistmaker.db.data.entity.PlaylistedTrackEntity
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
        SELECT * FROM $PLAYLISTED_TRACKS_TABLE 
        WHERE playlistId = :playlistId ORDER BY id DESC
        """)
    suspend fun getTracksByPlaylistId(playlistId: Int): List<PlaylistedTrackEntity>

    @Query("""
        SELECT SUM(trackTimeMillis) FROM $PLAYLISTED_TRACKS_TABLE 
        WHERE playlistId = :playlistId
        """)
    suspend fun getPlaylistDuration(playlistId: Int): Long

    @Query("""
    DELETE FROM $PLAYLISTED_TRACKS_TABLE 
    WHERE trackId = :trackId AND playlistId = :playlistId
    """)
    suspend fun deleteTrackFromPlaylist(trackId: Int, playlistId: Int)
}