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

    @Query("SELECT trackId FROM $PLAYLISTED_TRACKS_TABLE WHERE playlistId = :playlistId")
    fun getPlaylistedTracksIdFlow(playlistId: Int): Flow<List<Int>>
}