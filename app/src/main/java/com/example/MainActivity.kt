package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.characters.CharacterSelectScreen
import com.example.ui.gameplay.GamePlayScreen
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.lobby.LobbyScreen
import com.example.ui.missions.DailyMissionsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DashRunnerApp()
            }
        }
    }
}

@Composable
fun DashRunnerApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val characters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val missions by viewModel.dailyMissions.collectAsStateWithLifecycle()
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()
    val todayScores by viewModel.todayScores.collectAsStateWithLifecycle()
    val lastRunSummary by viewModel.lastRunSummary.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                AppScreen.LOBBY -> {
                    LobbyScreen(
                        profile = playerProfile,
                        characters = characters,
                        missions = missions,
                        onStartRun = { viewModel.startRun() },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                AppScreen.GAMEPLAY -> {
                    GamePlayScreen(
                        engine = viewModel.gameEngine,
                        lastRunSummary = lastRunSummary,
                        onTick = { viewModel.onGameLoopTick(it) },
                        onRestart = { viewModel.startRun() },
                        onExitToLobby = { viewModel.navigateTo(AppScreen.LOBBY) }
                    )
                }
                AppScreen.CHARACTERS -> {
                    CharacterSelectScreen(
                        profile = playerProfile,
                        characters = characters,
                        onSelectCharacter = { viewModel.selectCharacter(it) },
                        onUnlockCharacter = { viewModel.unlockCharacter(it) },
                        onUpgradeCharacter = { viewModel.upgradeCharacter(it) },
                        onBack = { viewModel.navigateTo(AppScreen.LOBBY) }
                    )
                }
                AppScreen.MISSIONS -> {
                    DailyMissionsScreen(
                        profile = playerProfile,
                        missions = missions,
                        onClaimReward = { viewModel.claimMission(it) },
                        onBack = { viewModel.navigateTo(AppScreen.LOBBY) }
                    )
                }
                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        profile = playerProfile,
                        topScores = topScores,
                        todayScores = todayScores,
                        onBack = { viewModel.navigateTo(AppScreen.LOBBY) }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        profile = playerProfile,
                        onToggleSound = { viewModel.toggleSound(it) },
                        onToggleHaptics = { viewModel.toggleHaptics(it) },
                        onBack = { viewModel.navigateTo(AppScreen.LOBBY) }
                    )
                }
            }
        }
    }
}

// Retained for unit/screenshot test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
