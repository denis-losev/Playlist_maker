package com.practicum.playlistmaker.db.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.practicum.playlistmaker.Constants.TRACK_TABLE

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE $TRACK_TABLE ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0"
        )
    }
}