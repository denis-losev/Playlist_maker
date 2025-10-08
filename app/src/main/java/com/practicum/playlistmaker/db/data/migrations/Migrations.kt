package com.practicum.playlistmaker.db.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.practicum.playlistmaker.Constants.PLAYLISTED_TRACKS_TABLE
import com.practicum.playlistmaker.Constants.PLAYLIST_TABLE
import com.practicum.playlistmaker.Constants.TRACK_TABLE

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE $TRACK_TABLE ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $PLAYLIST_TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                coverImage TEXT NOT NULL,
                tracksCount INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $PLAYLIST_TABLE ADD COLUMN description TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE $PLAYLIST_TABLE ADD COLUMN tracksIds TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE $PLAYLIST_TABLE RENAME COLUMN coverImage TO coverImage")
        database.execSQL("ALTER TABLE $PLAYLIST_TABLE RENAME COLUMN tracksCount TO tracksCount")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $PLAYLISTED_TRACKS_TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trackId INTEGER NOT NULL DEFAULT 0,
                playlistId INTEGER NOT NULL DEFAULT 0,
                UNIQUE(trackId, playlistId)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE playlisted_tracks_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trackId INTEGER NOT NULL,
                playlistId INTEGER NOT NULL,
                trackName TEXT NOT NULL,
                artistName TEXT NOT NULL,
                trackTimeMillis INTEGER NOT NULL,
                artworkUrl100 TEXT NOT NULL,
                collectionName TEXT NOT NULL,
                releaseDate TEXT NOT NULL,
                primaryGenreName TEXT NOT NULL,
                country TEXT NOT NULL,
                previewUrl TEXT,
                addedAt INTEGER NOT NULL
            )
        """.trimIndent()
        )

        database.execSQL(
            """
            INSERT INTO playlisted_tracks_new (id, trackId, playlistId, trackName, artistName, trackTimeMillis, artworkUrl100, collectionName, releaseDate, primaryGenreName, country, previewUrl, addedAt)
            SELECT 
                id, 
                trackId, 
                playlistId, 
                '', '', 0, '', '', '', '', '', NULL, 0
            FROM $PLAYLISTED_TRACKS_TABLE
        """.trimIndent()
        )

        database.execSQL("DROP TABLE $PLAYLISTED_TRACKS_TABLE")

        database.execSQL("ALTER TABLE playlisted_tracks_new RENAME TO $PLAYLISTED_TRACKS_TABLE")

    }
}