package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET coins = :coins, gems = :gems WHERE id = 1")
    suspend fun updateCurrency(coins: Int, gems: Int)

    @Query("UPDATE player_profile SET selectedCharacterId = :characterId WHERE id = 1")
    suspend fun setSelectedCharacter(characterId: String)

    @Query("UPDATE player_profile SET soundEnabled = :sound, hapticsEnabled = :haptics WHERE id = 1")
    suspend fun updateAudioHaptics(sound: Boolean, haptics: Boolean)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters")
    suspend fun getAllCharactersSync(): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialCharacters(characters: List<CharacterEntity>)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)
}

@Dao
interface MissionDao {
    @Query("SELECT * FROM daily_missions WHERE dateKey = :dateKey")
    fun getMissionsForDate(dateKey: String): Flow<List<DailyMissionEntity>>

    @Query("SELECT * FROM daily_missions WHERE dateKey = :dateKey")
    suspend fun getMissionsForDateSync(dateKey: String): List<DailyMissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailyMissionEntity>)

    @Update
    suspend fun updateMission(mission: DailyMissionEntity)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC LIMIT :limit")
    fun getTopScores(limit: Int = 50): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE timestamp >= :startTimestamp ORDER BY score DESC LIMIT :limit")
    fun getTodayScores(startTimestamp: Long, limit: Int = 50): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LeaderboardEntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialEntries(entries: List<LeaderboardEntryEntity>)

    @Query("SELECT MAX(score) FROM leaderboard_entries WHERE isLocalPlayer = 1")
    suspend fun getLocalPlayerBestScore(): Int?
}
