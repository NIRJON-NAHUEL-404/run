package com.example.ui.missions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyMissionEntity
import com.example.data.local.entity.PlayerProfileEntity

@Composable
fun DailyMissionsScreen(
    profile: PlayerProfileEntity?,
    missions: List<DailyMissionEntity>,
    onClaimReward: (DailyMissionEntity) -> Unit,
    onBack: () -> Unit
) {
    val completedCount = missions.count { it.isClaimed }
    val totalCount = missions.size

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
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DAILY MISSIONS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Reset timer badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resets Daily",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Completion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "$completedCount / $totalCount Claimed",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val overallProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF231E3D)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Complete your daily training runs to earn bonus gold coins and rare diamonds!",
                        color = Color(0xFFAAA5C8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Missions list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(missions, key = { it.id }) { mission ->
                    MissionCardItem(
                        mission = mission,
                        onClaim = { onClaimReward(mission) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MissionCardItem(
    mission: DailyMissionEntity,
    onClaim: () -> Unit
) {
    val isComplete = mission.currentCount >= mission.targetCount
    val progress = (mission.currentCount.toFloat() / mission.targetCount.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_item_${mission.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isClaimed) Color(0xFF131124) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (mission.isClaimed) Color(0xFF262040)
            else if (isComplete) Color(0xFF00E676).copy(alpha = 0.6f)
            else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (mission.isClaimed) Color(0xFFAAA5C8) else Color.White
                    )
                    Text(
                        text = mission.description,
                        fontSize = 12.sp,
                        color = Color(0xFF8E88B0)
                    )
                }

                // Reward badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mission.rewardCoins > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2A2210)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+${mission.rewardCoins}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (mission.rewardGems > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F2633)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💎", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+${mission.rewardGems}",
                                    color = Color(0xFF00F5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar and Claim button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 11.sp,
                            color = Color(0xFFAAA5C8)
                        )
                        Text(
                            text = "${mission.currentCount} / ${mission.targetCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isComplete) Color(0xFF00E676) else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (isComplete) Color(0xFF00E676) else Color(0xFF00F5FF),
                        trackColor = Color(0xFF1E1A38)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                if (mission.isClaimed) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1C2220)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Claimed",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CLAIMED",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onClaim,
                        enabled = isComplete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            disabledContainerColor = Color(0xFF26223B)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (isComplete) "CLAIM" else "IN PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isComplete) Color.Black else Color(0xFF7A7596)
                        )
                    }
                }
            }
        }
    }
}
