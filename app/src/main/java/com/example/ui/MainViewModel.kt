package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.repository.GameRepository
import com.example.game.engine.GameEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LOBBY,
    GAMEPLAY,
    CHARACTERS,
    MISSIONS,
    LEADERBOARD,
    SETTINGS
}

data class LastRunSummary(
    val score: Int,
    val distanceMeters: Int,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val stageReached: String,
    val isNewHighScore: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = GameRepository(database)
    val soundManager = SoundManager(application)
    val gameEngine = GameEngine(soundManager)

    private val _currentScreen = MutableStateFlow(AppScreen.LOBBY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _lastRunSummary = MutableStateFlow<LastRunSummary?>(null)
    val lastRunSummary: StateFlow<LastRunSummary?> = _lastRunSummary.asStateFlow()

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCharacters: StateFlow<List<CharacterEntity>> = repository.allCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topScores: StateFlow<List<LeaderboardEntryEntity>> = repository.topScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayScores: StateFlow<List<LeaderboardEntryEntity>> = repository.getTodayScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyMissions: StateFlow<List<DailyMissionEntity>> = repository.getTodayMissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
        viewModelScope.launch {
            playerProfile.collect { profile ->
                profile?.let {
                    soundManager.isSoundEnabled = it.soundEnabled
                    soundManager.isHapticsEnabled = it.hapticsEnabled
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playButtonClick()
        _currentScreen.value = screen
    }

    fun startRun() {
        val chars = allCharacters.value
        val selectedId = playerProfile.value?.selectedCharacterId ?: "aero"
        val activeChar = chars.firstOrNull { it.id == selectedId } ?: chars.firstOrNull()
        if (activeChar != null) {
            gameEngine.startGame(activeChar)
            _lastRunSummary.value = null
            _currentScreen.value = AppScreen.GAMEPLAY
        }
    }

    fun onGameLoopTick(dt: Float) {
        if (gameEngine.isPlaying && !gameEngine.isPaused) {
            gameEngine.update(dt)
            if (gameEngine.isGameOver) {
                handleGameOver()
            }
        }
    }

    private fun handleGameOver() {
        val score = gameEngine.score
        val distance = gameEngine.distanceMeters
        val coins = gameEngine.coinsCollected
        val gems = gameEngine.gemsCollected
        val stageName = gameEngine.currentStage.stageName
        val charName = gameEngine.currentCharacter?.name ?: "Runner"
        val isNewRecord = score > (playerProfile.value?.highScore ?: 0)

        _lastRunSummary.value = LastRunSummary(
            score = score,
            distanceMeters = distance,
            coinsEarned = coins,
            gemsEarned = gems,
            stageReached = stageName,
            isNewHighScore = isNewRecord
        )

        viewModelScope.launch {
            repository.recordRunResults(
                runScore = score,
                distanceMeters = distance,
                coinsEarned = coins,
                gemsEarned = gems,
                jumpsDone = gameEngine.jumpsCount,
                slidesDone = gameEngine.slidesCount,
                maxStageIndex = gameEngine.maxStageIndex,
                characterName = charName,
                stageName = stageName
            )
        }
    }

    fun selectCharacter(charId: String) {
        soundManager.playButtonClick()
        viewModelScope.launch {
            repository.selectCharacter(charId)
        }
    }

    fun unlockCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            val success = repository.unlockCharacter(character)
            if (success) {
                soundManager.playPowerUpSound()
            }
        }
    }

    fun upgradeCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            val success = repository.upgradeCharacter(character)
            if (success) {
                soundManager.playPowerUpSound()
            }
        }
    }

    fun claimMission(mission: DailyMissionEntity) {
        viewModelScope.launch {
            repository.claimMissionReward(mission)
            soundManager.playPowerUpSound()
        }
    }

    fun toggleSound(enabled: Boolean) {
        val currentHaptics = playerProfile.value?.hapticsEnabled ?: true
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(enabled, currentHaptics)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        val currentSound = playerProfile.value?.soundEnabled ?: true
        soundManager.isHapticsEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(currentSound, enabled)
        }
    }
}
