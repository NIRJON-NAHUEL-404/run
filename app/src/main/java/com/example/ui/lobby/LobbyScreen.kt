package com.example.ui.lobby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.ui.AppScreen

@Composable
fun LobbyScreen(
    profile: PlayerProfileEntity?,
    characters: List<CharacterEntity>,
    missions: List<DailyMissionEntity>,
    onStartRun: () -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    val selectedId = profile?.selectedCharacterId ?: "aero"
    val activeCharacter = characters.firstOrNull { it.id == selectedId }
        ?: characters.firstOrNull()

    val pendingMissionsCount = missions.count { it.currentCount >= it.targetCount && !it.isClaimed }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Offline Mode indicator, Currency (Coins, Gems), Settings Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offline status badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B2A1E),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline Mode Enabled",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OFFLINE READY",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Currency row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Coins
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile?.coins ?: 0}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Gems
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, Color(0xFF00F5FF).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile?.gems ?: 0}",
                                color = Color(0xFF00F5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Visual Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lobby_hero),
                        contentDescription = "Game Arena Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC0A0718),
                                        Color(0xF00A0718)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "SEASON 1 • HYPER SPEED",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DASH RUNNER",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Best Distance: ${profile?.bestDistanceMeters ?: 0}m  |  High Score: ${profile?.highScore ?: 0}",
                            color = Color(0xFFCCC8E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Character Showcase Card
            activeCharacter?.let { hero ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.CHARACTERS) }
                        .testTag("character_showcase_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, Color(hero.primaryColorHex).copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    color = Color(hero.primaryColorHex).copy(alpha = 0.2f),
                                    border = BorderStroke(2.dp, Color(hero.primaryColorHex))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = hero.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = Color(hero.primaryColorHex)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = hero.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${hero.title} • Lv. ${hero.level}",
                                        fontSize = 12.sp,
                                        color = Color(hero.primaryColorHex),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Switch character pill button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "SWITCH ❯",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hero Perks row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatPill(
                                icon = Icons.Default.Speed,
                                label = "Speed",
                                value = "+${hero.speedBonusPct}%",
                                color = Color(0xFF00F5FF)
                            )
                            StatPill(
                                icon = Icons.Default.FlashOn,
                                label = "Magnet",
                                value = "+${hero.magnetBonusPct}%",
                                color = Color(0xFFFF007F)
                            )
                            StatPill(
                                icon = Icons.Default.Security,
                                label = "Shield",
                                value = "+${hero.shieldBonusPct}%",
                                color = Color(0xFF00E676)
                            )
                            StatPill(
                                icon = Icons.Default.Star,
                                label = "Points",
                                value = "${hero.scoreMultiplierBonus}x",
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Action PLAY Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onStartRun,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .scale(pulseScale)
                        .testTag("start_run_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00F5FF),
                                        Color(0xFF8A2BE2),
                                        Color(0xFFFF007F)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START RUN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Hub Cards (Characters, Missions, Leaderboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Characters Card
                LobbyHubCard(
                    modifier = Modifier.weight(1f),
                    title = "HEROES",
                    subtitle = "${characters.count { it.isUnlocked }}/${characters.size} Ready",
                    icon = Icons.Default.MilitaryTech,
                    color = Color(0xFF00F5FF),
                    badgeCount = 0,
                    onClick = { onNavigate(AppScreen.CHARACTERS) }
                )

                // Daily Missions Card
                LobbyHubCard(
                    modifier = Modifier.weight(1f),
                    title = "MISSIONS",
                    subtitle = "${missions.count { it.isClaimed }}/${missions.size} Done",
                    icon = Icons.Default.FlashOn,
                    color = Color(0xFFFFD700),
                    badgeCount = pendingMissionsCount,
                    onClick = { onNavigate(AppScreen.MISSIONS) }
                )

                // Leaderboard Card
                LobbyHubCard(
                    modifier = Modifier.weight(1f),
                    title = "RANKS",
                    subtitle = "Highscores",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFF007F),
                    badgeCount = 0,
                    onClick = { onNavigate(AppScreen.LEADERBOARD) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1A38)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color(0xFFAAA5C8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun LobbyHubCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color(0xFFCCC8E5)
                    )
                }
            }

            if (badgeCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF2A4B)
                ) {
                    Text(
                        text = "$badgeCount",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
