package com.example.ui.characters

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.PlayerProfileEntity

@Composable
fun CharacterSelectScreen(
    profile: PlayerProfileEntity?,
    characters: List<CharacterEntity>,
    onSelectCharacter: (String) -> Unit,
    onUnlockCharacter: (CharacterEntity) -> Unit,
    onUpgradeCharacter: (CharacterEntity) -> Unit,
    onBack: () -> Unit
) {
    val selectedId = profile?.selectedCharacterId ?: "aero"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Lobby",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HERO ROSTER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Balance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile?.coins ?: 0}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💎", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile?.gems ?: 0}",
                                color = Color(0xFF00F5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select your runner and upgrade their skills for the race.",
                color = Color(0xFFAAA5C8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Character Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(characters, key = { it.id }) { hero ->
                    val isSelected = hero.id == selectedId
                    val canAffordUnlock = (profile?.coins ?: 0) >= hero.costCoins &&
                            (profile?.gems ?: 0) >= hero.costGems
                    val canAffordUpgrade = (profile?.coins ?: 0) >= hero.upgradeCostCoins

                    CharacterCardItem(
                        hero = hero,
                        isSelected = isSelected,
                        canAffordUnlock = canAffordUnlock,
                        canAffordUpgrade = canAffordUpgrade,
                        onSelect = { onSelectCharacter(hero.id) },
                        onUnlock = { onUnlockCharacter(hero) },
                        onUpgrade = { onUpgradeCharacter(hero) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacterCardItem(
    hero: CharacterEntity,
    isSelected: Boolean,
    canAffordUnlock: Boolean,
    canAffordUpgrade: Boolean,
    onSelect: () -> Unit,
    onUnlock: () -> Unit,
    onUpgrade: () -> Unit
) {
    val heroColor = Color(hero.primaryColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hero.isUnlocked) { onSelect() }
            .testTag("character_item_${hero.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) heroColor else heroColor.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, Level / Lock, Selected Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = heroColor.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, heroColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (hero.isUnlocked) {
                                Text(
                                    text = hero.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = heroColor
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = heroColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                            text = if (hero.isUnlocked) "${hero.title} • Level ${hero.level}" else hero.title,
                            fontSize = 12.sp,
                            color = heroColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = heroColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, heroColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = heroColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE",
                                color = heroColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = hero.description,
                color = Color(0xFFC0BCDC),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Progress Bars
            StatProgressBar(label = "Speed Bonus", value = hero.speedBonusPct, max = 30, color = Color(0xFF00F5FF))
            Spacer(modifier = Modifier.height(6.dp))
            StatProgressBar(label = "Magnet Radius", value = hero.magnetBonusPct, max = 50, color = Color(0xFFFF007F))
            Spacer(modifier = Modifier.height(6.dp))
            StatProgressBar(label = "Shield Boost", value = hero.shieldBonusPct, max = 60, color = Color(0xFF00E676))
            Spacer(modifier = Modifier.height(6.dp))
            StatProgressBar(label = "Score Multiplier", value = (hero.scoreMultiplierBonus * 20).toInt(), max = 40, color = Color(0xFFFFD700))

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (hero.isUnlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isSelected) {
                        Button(
                            onClick = onSelect,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = heroColor)
                        ) {
                            Text(
                                text = "SELECT",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Upgrade Button
                    OutlinedButton(
                        onClick = onUpgrade,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = canAffordUpgrade,
                        border = BorderStroke(1.dp, if (canAffordUpgrade) Color(0xFFFFD700) else Color.Gray)
                    ) {
                        Text(
                            text = "UPGRADE 🪙${hero.upgradeCostCoins}",
                            fontWeight = FontWeight.Bold,
                            color = if (canAffordUpgrade) Color(0xFFFFD700) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Unlock Button
                Button(
                    onClick = onUnlock,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    enabled = canAffordUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAffordUnlock) heroColor else Color(0xFF332F4A)
                    )
                ) {
                    val costText = buildString {
                        append("UNLOCK FOR ")
                        if (hero.costCoins > 0) append("🪙 ${hero.costCoins} ")
                        if (hero.costGems > 0) append("💎 ${hero.costGems}")
                    }
                    Text(
                        text = costText,
                        fontWeight = FontWeight.Bold,
                        color = if (canAffordUnlock) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun StatProgressBar(label: String, value: Int, max: Int, color: Color) {
    val progress = (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFAAA5C8),
            modifier = Modifier.width(105.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = color,
            trackColor = Color(0xFF221E3A),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$value%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(36.dp)
        )
    }
}
