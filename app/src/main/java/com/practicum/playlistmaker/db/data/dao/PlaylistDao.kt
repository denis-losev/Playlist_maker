package com.practicum.playlistmaker.db.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.practicum.playlistmaker.Constants.PLAYLIST_TABLE
import com.practicum.playlistmaker.db.data.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM $PLAYLIST_TABLE")
    fun getPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Query("SELECT tracksCount from $PLAYLIST_TABLE WHERE name = :name")
    suspend fun getTracksCount(name: String): Int

    @Query("UPDATE $PLAYLIST_TABLE SET tracksCount = :tracksCount WHERE name = :playlistName")
    suspend fun setTracksCount(playlistName: String, tracksCount: Int): Int

    @Query("SELECT id FROM $PLAYLIST_TABLE WHERE name = :playlistName")
    suspend fun getPlaylistId(playlistName: String): Int

    @Update
    suspend fun updatePlaylist(playlistEntity: PlaylistEntity)
}