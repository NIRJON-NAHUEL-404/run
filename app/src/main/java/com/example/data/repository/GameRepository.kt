package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(private val database: AppDatabase) {
    private val playerDao = database.playerDao()
    private val characterDao = database.characterDao()
    private val missionDao = database.missionDao()
    private val leaderboardDao = database.leaderboardDao()

    val playerProfile: Flow<PlayerProfileEntity?> = playerDao.getPlayerProfile()
    val allCharacters: Flow<List<CharacterEntity>> = characterDao.getAllCharacters()
    val topScores: Flow<List<LeaderboardEntryEntity>> = leaderboardDao.getTopScores(50)

    fun getTodayScores(): Flow<List<LeaderboardEntryEntity>> {
        val startOfToday = getStartOfTodayTimestamp()
        return leaderboardDao.getTodayScores(startOfToday, 50)
    }

    fun getTodayMissions(): Flow<List<DailyMissionEntity>> {
        val todayKey = getTodayDateKey()
        return missionDao.getMissionsForDate(todayKey)
    }

    suspend fun initializeDefaultsIfNeeded() {
        // 1. Initialize Player
        val currentProfile = playerDao.getPlayerProfileSync()
        if (currentProfile == null) {
            playerDao.insertOrUpdate(
                PlayerProfileEntity(
                    id = 1,
                    playerName = "CyberRunner",
                    coins = 200,
                    gems = 10,
                    selectedCharacterId = "aero"
                )
            )
        }

        // 2. Initialize Characters
        val defaultCharacters = listOf(
            CharacterEntity(
                id = "aero",
                name = "Aero Sprint",
                title = "Agile Scout",
                description = "Balanced runner with high agility and quick recovery.",
                isUnlocked = true,
                costCoins = 0,
                costGems = 0,
                speedBonusPct = 5,
                magnetBonusPct = 10,
                shieldBonusPct = 0,
                scoreMultiplierBonus = 1.0f,
                level = 1,
                upgradeCostCoins = 150,
                primaryColorHex = 0xFF00F5FF,
                secondaryColorHex = 0xFF00A3FF
            ),
            CharacterEntity(
                id = "neon_blade",
                name = "Neon Blade",
                title = "Cyber Ninja",
                description = "Equipped with magnetic coils. Pulls coins from greater distances.",
                isUnlocked = false,
                costCoins = 400,
                costGems = 0,
                speedBonusPct = 10,
                magnetBonusPct = 35,
                shieldBonusPct = 10,
                scoreMultiplierBonus = 1.2f,
                level = 1,
                upgradeCostCoins = 250,
                primaryColorHex = 0xFFFF007F,
                secondaryColorHex = 0xFFFF77A9
            ),
            CharacterEntity(
                id = "valkyrie",
                name = "Valkyrie Sky",
                title = "Jetpack Ace",
                description = "Energy shielding lasts 40% longer and jump gravity is lighter.",
                isUnlocked = false,
                costCoins = 750,
                costGems = 15,
                speedBonusPct = 15,
                magnetBonusPct = 15,
                shieldBonusPct = 40,
                scoreMultiplierBonus = 1.35f,
                level = 1,
                upgradeCostCoins = 350,
                primaryColorHex = 0xFFFFD700,
                secondaryColorHex = 0xFFFF9900
            ),
            CharacterEntity(
                id = "shadow",
                name = "Shadow Phantom",
                title = "Quantum Ghost",
                description = "Gains double score multiplier duration and extra base point multiplier.",
                isUnlocked = false,
                costCoins = 1200,
                costGems = 25,
                speedBonusPct = 20,
                magnetBonusPct = 20,
                shieldBonusPct = 15,
                scoreMultiplierBonus = 1.5f,
                level = 1,
                upgradeCostCoins = 500,
                primaryColorHex = 0xFFBD00FF,
                secondaryColorHex = 0xFF7000FF
            ),
            CharacterEntity(
                id = "titan",
                name = "Titan Golem",
                title = "Heavy Juggernaut",
                description = "Armored powerhouse. Automatically begins every run with an Energy Shield!",
                isUnlocked = false,
                costCoins = 2000,
                costGems = 40,
                speedBonusPct = 10,
                magnetBonusPct = 25,
                shieldBonusPct = 50,
                scoreMultiplierBonus = 1.65f,
                level = 1,
                upgradeCostCoins = 650,
                primaryColorHex = 0xFF00E676,
                secondaryColorHex = 0xFF00B0FF
            )
        )
        characterDao.insertInitialCharacters(defaultCharacters)

        // 3. Initialize Daily Missions
        ensureDailyMissions()

        // 4. Initialize Offline Leaderboard Rivals
        val initialLeaderboard = listOf(
            LeaderboardEntryEntity(
                playerName = "ApexRunner",
                score = 14520,
                distanceMeters = 2140,
                characterName = "Titan Golem",
                stageReached = "Quantum Cosmos",
                timestamp = System.currentTimeMillis() - 3600_000 * 5
            ),
            LeaderboardEntryEntity(
                playerName = "CyberFox",
                score = 11840,
                distanceMeters = 1780,
                characterName = "Shadow Phantom",
                stageReached = "Quantum Cosmos",
                timestamp = System.currentTimeMillis() - 3600_000 * 12
            ),
            LeaderboardEntryEntity(
                playerName = "NeonViper",
                score = 8920,
                distanceMeters = 1350,
                characterName = "Neon Blade",
                stageReached = "Volcano Cavern",
                timestamp = System.currentTimeMillis() - 3600_000 * 2
            ),
            LeaderboardEntryEntity(
                playerName = "PulseRider",
                score = 6450,
                distanceMeters = 980,
                characterName = "Valkyrie Sky",
                stageReached = "Volcano Cavern",
                timestamp = System.currentTimeMillis() - 3600_000 * 18
            ),
            LeaderboardEntryEntity(
                playerName = "SpeedDemon",
                score = 4820,
                distanceMeters = 720,
                characterName = "Aero Sprint",
                stageReached = "Mystic Jungle",
                timestamp = System.currentTimeMillis() - 3600_000 * 1
            ),
            LeaderboardEntryEntity(
                playerName = "PixelGhost",
                score = 3150,
                distanceMeters = 490,
                characterName = "Aero Sprint",
                stageReached = "Mystic Jungle",
                timestamp = System.currentTimeMillis() - 3600_000 * 24
            ),
            LeaderboardEntryEntity(
                playerName = "MatrixVolt",
                score = 1820,
                distanceMeters = 290,
                characterName = "Aero Sprint",
                stageReached = "Cyber Metro",
                timestamp = System.currentTimeMillis() - 3600_000 * 4
            )
        )
        leaderboardDao.insertInitialEntries(initialLeaderboard)
    }

    suspend fun ensureDailyMissions() {
        val todayKey = getTodayDateKey()
        val existing = missionDao.getMissionsForDateSync(todayKey)
        if (existing.isEmpty()) {
            val missions = listOf(
                DailyMissionEntity(
                    id = "dist_500_$todayKey",
                    title = "Endurance Sprint",
                    description = "Run 400 meters in a single sprint",
                    targetCount = 400,
                    currentCount = 0,
                    rewardCoins = 150,
                    rewardGems = 2,
                    isClaimed = false,
                    dateKey = todayKey,
                    missionType = "DISTANCE"
                ),
                DailyMissionEntity(
                    id = "coins_50_$todayKey",
                    title = "Coin Hoarder",
                    description = "Collect 40 gold coins across runs",
                    targetCount = 40,
                    currentCount = 0,
                    rewardCoins = 120,
                    rewardGems = 1,
                    isClaimed = false,
                    dateKey = todayKey,
                    missionType = "COINS"
                ),
                DailyMissionEntity(
                    id = "jumps_10_$todayKey",
                    title = "Acrobatic Leap",
                    description = "Jump over 10 hurdles or barriers",
                    targetCount = 10,
                    currentCount = 0,
                    rewardCoins = 100,
                    rewardGems = 1,
                    isClaimed = false,
                    dateKey = todayKey,
                    missionType = "JUMPS"
                ),
                DailyMissionEntity(
                    id = "slides_6_$todayKey",
                    title = "Low Clearance",
                    description = "Slide under 6 low laser beams or vines",
                    targetCount = 6,
                    currentCount = 0,
                    rewardCoins = 110,
                    rewardGems = 1,
                    isClaimed = false,
                    dateKey = todayKey,
                    missionType = "SLIDES"
                ),
                DailyMissionEntity(
                    id = "stage_2_$todayKey",
                    title = "Jungle Pioneer",
                    description = "Survive to reach Stage 2: Mystic Jungle",
                    targetCount = 1,
                    currentCount = 0,
                    rewardCoins = 200,
                    rewardGems = 3,
                    isClaimed = false,
                    dateKey = todayKey,
                    missionType = "STAGE"
                )
            )
            missionDao.insertMissions(missions)
        }
    }

    suspend fun selectCharacter(characterId: String) {
        playerDao.setSelectedCharacter(characterId)
    }

    suspend fun unlockCharacter(character: CharacterEntity): Boolean {
        val profile = playerDao.getPlayerProfileSync() ?: return false
        if (profile.coins >= character.costCoins && profile.gems >= character.costGems) {
            val newCoins = profile.coins - character.costCoins
            val newGems = profile.gems - character.costGems
            playerDao.updateCurrency(newCoins, newGems)
            characterDao.updateCharacter(character.copy(isUnlocked = true))
            playerDao.setSelectedCharacter(character.id)
            return true
        }
        return false
    }

    suspend fun upgradeCharacter(character: CharacterEntity): Boolean {
        val profile = playerDao.getPlayerProfileSync() ?: return false
        if (profile.coins >= character.upgradeCostCoins) {
            val newCoins = profile.coins - character.upgradeCostCoins
            playerDao.updateCurrency(newCoins, profile.gems)
            val nextLevel = character.level + 1
            characterDao.updateCharacter(
                character.copy(
                    level = nextLevel,
                    upgradeCostCoins = character.upgradeCostCoins + 150,
                    speedBonusPct = character.speedBonusPct + 2,
                    magnetBonusPct = character.magnetBonusPct + 5,
                    shieldBonusPct = character.shieldBonusPct + 5,
                    scoreMultiplierBonus = character.scoreMultiplierBonus + 0.05f
                )
            )
            return true
        }
        return false
    }

    suspend fun claimMissionReward(mission: DailyMissionEntity) {
        if (mission.isClaimed || mission.currentCount < mission.targetCount) return
        val profile = playerDao.getPlayerProfileSync() ?: return
        val updatedCoins = profile.coins + mission.rewardCoins
        val updatedGems = profile.gems + mission.rewardGems
        playerDao.updateCurrency(updatedCoins, updatedGems)
        missionDao.updateMission(mission.copy(isClaimed = true))
    }

    suspend fun recordRunResults(
        runScore: Int,
        distanceMeters: Int,
        coinsEarned: Int,
        gemsEarned: Int,
        jumpsDone: Int,
        slidesDone: Int,
        maxStageIndex: Int,
        characterName: String,
        stageName: String
    ) {
        val profile = playerDao.getPlayerProfileSync() ?: return
        val newCoins = profile.coins + coinsEarned
        val newGems = profile.gems + gemsEarned
        val newHighScore = maxOf(profile.highScore, runScore)
        val newBestDistance = maxOf(profile.bestDistanceMeters, distanceMeters)
        val updatedProfile = profile.copy(
            coins = newCoins,
            gems = newGems,
            highScore = newHighScore,
            bestDistanceMeters = newBestDistance,
            totalRuns = profile.totalRuns + 1,
            totalCoinsCollected = profile.totalCoinsCollected + coinsEarned
        )
        playerDao.insertOrUpdate(updatedProfile)

        // Record on leaderboard
        leaderboardDao.insertEntry(
            LeaderboardEntryEntity(
                playerName = profile.playerName,
                score = runScore,
                distanceMeters = distanceMeters,
                characterName = characterName,
                stageReached = stageName,
                timestamp = System.currentTimeMillis(),
                isLocalPlayer = true
            )
        )

        // Update Daily Missions progress
        val todayKey = getTodayDateKey()
        val missions = missionDao.getMissionsForDateSync(todayKey)
        for (m in missions) {
            if (m.isClaimed) continue
            var updatedCount = m.currentCount
            when (m.missionType) {
                "DISTANCE" -> updatedCount = maxOf(updatedCount, distanceMeters)
                "COINS" -> updatedCount = minOf(m.targetCount, updatedCount + coinsEarned)
                "GEMS" -> updatedCount = minOf(m.targetCount, updatedCount + gemsEarned)
                "JUMPS" -> updatedCount = minOf(m.targetCount, updatedCount + jumpsDone)
                "SLIDES" -> updatedCount = minOf(m.targetCount, updatedCount + slidesDone)
                "STAGE" -> {
                    if (maxStageIndex >= 1) { // 1 = Stage 2
                        updatedCount = 1
                    }
                }
            }
            if (updatedCount != m.currentCount) {
                missionDao.updateMission(m.copy(currentCount = updatedCount))
            }
        }
    }

    suspend fun updateSettings(sound: Boolean, haptics: Boolean) {
        playerDao.updateAudioHaptics(sound, haptics)
    }

    private fun getTodayDateKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getStartOfTodayTimestamp(): Long {
        val now = System.currentTimeMillis()
        val offset = now % (24 * 3600_000L)
        return now - offset
    }
}
