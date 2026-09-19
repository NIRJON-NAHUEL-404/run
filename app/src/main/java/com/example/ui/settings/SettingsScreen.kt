package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlayerProfileEntity

@Composable
fun SettingsScreen(
    profile: PlayerProfileEntity?,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val soundEnabled = profile?.soundEnabled ?: true
    val hapticsEnabled = profile?.hapticsEnabled ?: true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SETTINGS & GUIDE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Haptics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIO & FEEDBACK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F5FF),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Game Sound Effects",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Synthesized real-time audio",
                                    fontSize = 11.sp,
                                    color = Color(0xFFAAA5C8)
                                )
                            }
                        }

                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = onToggleSound,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00F5FF),
                                checkedTrackColor = Color(0xFF005662)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptics switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Vibration & Haptics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tactile bump on coin, jump & collision",
                                    fontSize = 11.sp,
                                    color = Color(0xFFAAA5C8)
                                )
                            }
                        }

                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = onToggleHaptics,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF007F),
                                checkedTrackColor = Color(0xFF6B0037)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // How to Play Controls Guide
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Swipe,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HOW TO PLAY & CONTROLS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ControlItem(
                        key = "SWIPE LEFT / RIGHT",
                        desc = "Switch between the 3 running lanes to dodge spikes, blocks, and gather coins."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ControlItem(
                        key = "SWIPE UP (OR TAP JUMP)",
                        desc = "Leap over High Hurdles, Ancient Totems, and Magma Geysers."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ControlItem(
                        key = "SWIPE DOWN (OR TAP SLIDE)",
                        desc = "Crouch slide beneath Low Laser Beams, Hanging Vines, and Stalactites."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lifetime Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIFETIME CAREER STATS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CareerStatItem("Total Runs", "${profile?.totalRuns ?: 0}")
                        CareerStatItem("Best Distance", "${profile?.bestDistanceMeters ?: 0}m")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CareerStatItem("Total Coins", "${profile?.totalCoinsCollected ?: 0}")
                        CareerStatItem("High Score", "${profile?.highScore ?: 0}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline Mode Guarantee
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131E17),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Offline Compatible",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "All profiles, missions, characters, and leaderboards are stored securely on your device.",
                            fontSize = 11.sp,
                            color = Color(0xFFC0D6C5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ControlItem(key: String, desc: String) {
    Column {
        Text(
            text = key,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = desc,
            fontSize = 11.sp,
            color = Color(0xFFAAA5C8)
        )
    }
}

@Composable
private fun CareerStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFAAA5C8)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
