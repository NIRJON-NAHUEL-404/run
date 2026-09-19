package com.example.ui.leaderboard

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.PlayerProfileEntity

@Composable
fun LeaderboardScreen(
    profile: PlayerProfileEntity?,
    topScores: List<LeaderboardEntryEntity>,
    todayScores: List<LeaderboardEntryEntity>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentList = if (selectedTab == 0) topScores else todayScores

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
                    text = "HALL OF FAME",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: All-Time vs Today
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF00F5FF),
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00F5FF)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "ALL-TIME LEGENDS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) Color(0xFF00F5FF) else Color(0xFFAAA5C8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "TODAY'S RUNS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) Color(0xFF00F5FF) else Color(0xFFAAA5C8)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leaderboard list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(currentList, key = { index, item -> "${item.id}_$index" }) { index, entry ->
                    LeaderboardRowItem(
                        rank = index + 1,
                        entry = entry
                    )
                }

                if (currentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No runs recorded yet today. Start your first sprint!",
                                color = Color(0xFFAAA5C8),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRowItem(
    rank: Int,
    entry: LeaderboardEntryEntity
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF867EA6)
    }

    val isTop3 = rank <= 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_item_$rank"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isLocalPlayer) Color(0xFF1B1438) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (entry.isLocalPlayer) Color(0xFF00F5FF).copy(alpha = 0.7f)
            else if (isTop3) rankBadgeColor.copy(alpha = 0.35f)
            else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Number / Medal
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = rankBadgeColor.copy(alpha = if (isTop3) 0.2f else 0.1f),
                    border = BorderStroke(1.5.dp, rankBadgeColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isTop3) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = rankBadgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "$rank",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = rankBadgeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.playerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (entry.isLocalPlayer) Color(0xFF00F5FF) else Color.White
                        )
                        if (entry.isLocalPlayer) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00F5FF).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "YOU",
                                    color = Color(0xFF00F5FF),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${entry.characterName} • ${entry.stageReached}",
                        fontSize = 11.sp,
                        color = Color(0xFFAAA5C8)
                    )
                }
            }

            // Score and Distance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score} pts",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isTop3) rankBadgeColor else Color.White
                )
                Text(
                    text = "${entry.distanceMeters}m",
                    fontSize = 11.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
