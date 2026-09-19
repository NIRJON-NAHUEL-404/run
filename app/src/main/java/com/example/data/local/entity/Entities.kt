package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "CyberRunner",
    val coins: Int = 250,
    val gems: Int = 15,
    val selectedCharacterId: String = "aero",
    val highScore: Int = 0,
    val bestDistanceMeters: Int = 0,
    val totalRuns: Int = 0,
    val totalCoinsCollected: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val costCoins: Int,
    val costGems: Int,
    val speedBonusPct: Int,
    val magnetBonusPct: Int,
    val shieldBonusPct: Int,
    val scoreMultiplierBonus: Float,
    val level: Int = 1,
    val upgradeCostCoins: Int = 200,
    val primaryColorHex: Long = 0xFF00F5FF,
    val secondaryColorHex: Long = 0xFFFF007F
)

@Entity(tableName = "daily_missions")
data class DailyMissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val isClaimed: Boolean,
    val dateKey: String,
    val missionType: String // DISTANCE, COINS, GEMS, JUMPS, SLIDES, STAGE
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String,
    val score: Int,
    val distanceMeters: Int,
    val characterName: String,
    val stageReached: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLocalPlayer: Boolean = false
)
