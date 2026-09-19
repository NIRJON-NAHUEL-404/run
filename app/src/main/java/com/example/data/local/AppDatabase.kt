package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.LeaderboardDao
import com.example.data.local.dao.MissionDao
import com.example.data.local.dao.PlayerDao
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.PlayerProfileEntity

@Database(
    entities = [
        PlayerProfileEntity::class,
        CharacterEntity::class,
        DailyMissionEntity::class,
        LeaderboardEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun characterDao(): CharacterDao
    abstract fun missionDao(): MissionDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "runner_game.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
