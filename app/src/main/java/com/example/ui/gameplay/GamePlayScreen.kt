package com.example.ui.gameplay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.CollectibleItem
import com.example.game.model.CollectibleType
import com.example.game.model.Obstacle
import com.example.game.model.ObstacleType
import com.example.game.model.StageType
import com.example.ui.LastRunSummary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayScreen(
    engine: GameEngine,
    lastRunSummary: LastRunSummary?,
    onTick: (Float) -> Unit,
    onRestart: () -> Unit,
    onExitToLobby: () -> Unit
) {
    // 60FPS Game Loop Ticker
    LaunchedEffect(engine.isPlaying, engine.isPaused, engine.isGameOver) {
        var lastFrameTime = 0L
        while (engine.isPlaying && !engine.isPaused && !engine.isGameOver) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTime != 0L) {
                    val dt = (frameTimeNanos - lastFrameTime) / 1_000_000_000f
                    onTick(dt)
                }
                lastFrameTime = frameTimeNanos
            }
        }
    }

    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(engine.isPlaying) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumX = 0f
                        dragAccumY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumX += dragAmount.x
                        dragAccumY += dragAmount.y

                        val swipeThreshold = 45f
                        if (abs(dragAccumX) > swipeThreshold && abs(dragAccumX) > abs(dragAccumY)) {
                            if (dragAccumX > 0) {
                                engine.moveRight()
                            } else {
                                engine.moveLeft()
                            }
                            dragAccumX = 0f
                            dragAccumY = 0f
                        } else if (abs(dragAccumY) > swipeThreshold) {
                            if (dragAccumY < 0) {
                                engine.jump()
                            } else {
                                engine.slide()
                            }
                            dragAccumX = 0f
                            dragAccumY = 0f
                        }
                    }
                )
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Animated running time accumulator for runner animation
        var animTime by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos {
                    animTime += 0.016f
                }
            }
        }

        // Custom High Performance Canvas Renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stage = engine.currentStage

            // 1. Draw Horizon & Dynamic Sky
            drawStageSky(w, h, stage, animTime)

            // 2. Draw 3-Lane Perspective Runner Track
            val horizonY = h * 0.22f
            val groundY = h * 1.0f
            val topTrackWidth = w * 0.38f
            val bottomTrackWidth = w * 0.94f
            val centerX = w * 0.5f

            drawPerspectiveTrack(
                w = w,
                h = h,
                horizonY = horizonY,
                groundY = groundY,
                topWidth = topTrackWidth,
                bottomWidth = bottomTrackWidth,
                centerX = centerX,
                stage = stage,
                distance = engine.distanceMeters,
                animTime = animTime
            )

            // 3. Draw Collectibles (Coins, Gems, Power-ups)
            engine.collectibles.forEach { item ->
                drawCollectible(item, horizonY, groundY, topTrackWidth, bottomTrackWidth, centerX, animTime)
            }

            // 4. Draw Obstacles
            engine.obstacles.forEach { obs ->
                drawObstacle(obs, horizonY, groundY, topTrackWidth, bottomTrackWidth, centerX, stage)
            }

            // 5. Draw Runner Hero
            drawRunnerHero(
                engine = engine,
                horizonY = horizonY,
                groundY = groundY,
                topWidth = topTrackWidth,
                bottomWidth = bottomTrackWidth,
                centerX = centerX,
                animTime = animTime
            )

            // 6. Draw FX Particles
            engine.particles.forEach { p ->
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(p.color).copy(alpha = alpha),
                    radius = p.radius * alpha,
                    center = Offset(p.x * w, p.y * h)
                )
            }
        }

        // Top Heads-Up Display (HUD)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stage & Distance Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(engine.currentStage.primaryColor).copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(engine.currentStage.primaryColor))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${engine.distanceMeters}m",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = engine.currentStage.stageName.uppercase(),
                            color = Color(engine.currentStage.primaryColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Coins & Gems
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x99181230),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${engine.coinsCollected}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x99181230),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F5FF).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💎", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${engine.gemsCollected}",
                                color = Color(0xFF00F5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Pause Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0x99181230),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        IconButton(
                            onClick = { engine.pauseGame() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause Game",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score & Active Buffs row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCORE: ${engine.score}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                // Power up badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (engine.activeShield) {
                        PowerUpBadge("SHIELD", "%.0fs".format(engine.shieldTimer), Color(0xFF00E676))
                    }
                    if (engine.activeMagnet) {
                        PowerUpBadge("MAGNET", "%.0fs".format(engine.magnetTimer), Color(0xFFFF007F))
                    }
                    if (engine.activeMultiplier) {
                        PowerUpBadge("2X SCORE", "%.0fs".format(engine.multiplierTimer), Color(0xFFFFD700))
                    }
                    if (engine.activeBoost) {
                        PowerUpBadge("HYPER", "%.0fs".format(engine.boostTimer), Color(0xFF00F5FF))
                    }
                }
            }

            // Floating banner for Stage Upgrade or Announcements
            engine.stageBannerText?.let { banner ->
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xEE1A0A38),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(engine.currentStage.primaryColor))
                    ) {
                        Text(
                            text = banner,
                            color = Color(engine.currentStage.primaryColor),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // On-Screen Tactile Virtual Controls at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lane Change Buttons (Left & Right)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TactileControlButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDesc = "Move Left",
                        color = Color(0xFF00F5FF),
                        onClick = { engine.moveLeft() }
                    )
                    TactileControlButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDesc = "Move Right",
                        color = Color(0xFF00F5FF),
                        onClick = { engine.moveRight() }
                    )
                }

                // Action Buttons (Jump & Slide)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TactileControlButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDesc = "Jump",
                        label = "JUMP",
                        color = Color(0xFFFFD700),
                        onClick = { engine.jump() }
                    )
                    TactileControlButton(
                        icon = Icons.Default.ArrowDownward,
                        contentDesc = "Slide",
                        label = "SLIDE",
                        color = Color(0xFFFF007F),
                        onClick = { engine.slide() }
                    )
                }
            }
        }

        // Pause Modal Overlay
        if (engine.isPaused && !engine.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13102B)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F5FF))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { engine.resumeGame() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF))
                        ) {
                            Text("RESUME", fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text("RESTART RUN", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onExitToLobby,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text("LOBBY", color = Color.White)
                        }
                    }
                }
            }
        }

        // Game Over Modal Overlay
        if (engine.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF2A4B))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF2A4B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "RUN CRASHED",
                                color = Color(0xFFFF2A4B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (lastRunSummary?.isNewHighScore == true) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "★ NEW HIGH SCORE! ★",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = "${engine.score}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "TOTAL POINTS",
                            fontSize = 11.sp,
                            color = Color(0xFFAAA5C8),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RunStatCard("Distance", "${engine.distanceMeters}m", Color(0xFF00F5FF))
                            RunStatCard("Coins", "+${engine.coinsCollected}", Color(0xFFFFD700))
                            RunStatCard("Gems", "+${engine.gemsCollected}", Color(0xFF00F5FF))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Stage: ${engine.currentStage.stageName}",
                            color = Color(engine.currentStage.primaryColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("retry_run_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RUN AGAIN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onExitToLobby,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("lobby_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "BACK TO LOBBY",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TactileControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    label: String? = null,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xBB1A1435),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.7f)),
        modifier = Modifier.size(54.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDesc,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                label?.let {
                    Text(
                        text = it,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerUpBadge(title: String, duration: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = duration,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RunStatCard(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1D173A),
        modifier = Modifier.width(82.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFFAAA5C8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

// -------------------------------------------------------------
// Canvas Graphics Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawStageSky(w: Float, h: Float, stage: StageType, time: Float) {
    val horizonY = h * 0.22f

    val skyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF05020E),
            Color(stage.trackColor),
            Color(stage.primaryColor).copy(alpha = 0.25f)
        ),
        startY = 0f,
        endY = horizonY
    )
    drawRect(brush = skyGradient, size = Size(w, horizonY))

    // Glowing sun / core orb on horizon
    val sunRadius = w * 0.12f
    val sunCenter = Offset(w * 0.5f, horizonY - sunRadius * 0.2f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(stage.accentGlow).copy(alpha = 0.8f),
                Color(stage.primaryColor).copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = sunCenter,
            radius = sunRadius * 1.6f
        ),
        radius = sunRadius * 1.6f,
        center = sunCenter
    )
    drawCircle(
        color = Color(stage.primaryColor),
        radius = sunRadius * 0.7f,
        center = sunCenter
    )
}

private fun DrawScope.drawPerspectiveTrack(
    w: Float,
    h: Float,
    horizonY: Float,
    groundY: Float,
    topWidth: Float,
    bottomWidth: Float,
    centerX: Float,
    stage: StageType,
    distance: Int,
    animTime: Float
) {
    // 1. Road polygon
    val trackPath = Path().apply {
        moveTo(centerX - topWidth / 2f, horizonY)
        lineTo(centerX + topWidth / 2f, horizonY)
        lineTo(centerX + bottomWidth / 2f, groundY)
        lineTo(centerX - bottomWidth / 2f, groundY)
        close()
    }

    drawPath(
        path = trackPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(stage.trackColor),
                Color(stage.trackColor).copy(alpha = 0.95f),
                Color(0xFF0F0A24)
            ),
            startY = horizonY,
            endY = groundY
        )
    )

    // Outer track neon borders
    val borderGlow = Color(stage.primaryColor)
    drawLine(
        color = borderGlow,
        start = Offset(centerX - topWidth / 2f, horizonY),
        end = Offset(centerX - bottomWidth / 2f, groundY),
        strokeWidth = 3.5f
    )
    drawLine(
        color = borderGlow,
        start = Offset(centerX + topWidth / 2f, horizonY),
        end = Offset(centerX + bottomWidth / 2f, groundY),
        strokeWidth = 3.5f
    )

    // Lane dividers (2 lines separating 3 lanes)
    // Lane 0 = Left, Lane 1 = Center, Lane 2 = Right
    for (divider in 1..2) {
        val topX = (centerX - topWidth / 2f) + (topWidth / 3f) * divider
        val bottomX = (centerX - bottomWidth / 2f) + (bottomWidth / 3f) * divider

        // Dashed lane lines moving towards camera
        val segments = 10
        for (i in 0 until segments) {
            val progress = ((i / segments.toFloat()) + (animTime * 1.5f)) % 1.0f
            val p2 = (progress + 0.04f).coerceAtMost(1.0f)

            val x1 = topX + (bottomX - topX) * progress
            val y1 = horizonY + (groundY - horizonY) * progress
            val x2 = topX + (bottomX - topX) * p2
            val y2 = horizonY + (groundY - horizonY) * p2

            drawLine(
                color = Color.White.copy(alpha = 0.35f * progress),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f + progress * 4f
            )
        }
    }
}

private fun DrawScope.drawCollectible(
    item: CollectibleItem,
    horizonY: Float,
    groundY: Float,
    topWidth: Float,
    bottomWidth: Float,
    centerX: Float,
    animTime: Float
) {
    val progress = item.yPos.coerceIn(0f, 1.1f)
    val curTopWidth = topWidth + (bottomWidth - topWidth) * progress
    val curLeft = centerX - curTopWidth / 2f
    val laneW = curTopWidth / 3f
    val itemX = curLeft + laneW * item.lane + laneW * 0.5f
    val itemY = horizonY + (groundY - horizonY) * progress

    val scale = (0.35f + progress * 0.75f).coerceIn(0.2f, 1.2f)

    when (item.type) {
        CollectibleType.COIN -> {
            // Spinning golden coin
            val spin = cos(animTime * 6f + item.id).coerceIn(-1f, 1f)
            val coinW = 16.dp.toPx() * scale * abs(spin).coerceAtLeast(0.25f)
            val coinH = 16.dp.toPx() * scale

            // Gold Outer Glow
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                radius = 14.dp.toPx() * scale,
                center = Offset(itemX, itemY)
            )
            // Coin body
            drawOval(
                color = Color(0xFFFFD700),
                topLeft = Offset(itemX - coinW / 2f, itemY - coinH / 2f),
                size = Size(coinW, coinH)
            )
            drawOval(
                color = Color(0xFFFFA000),
                topLeft = Offset(itemX - coinW * 0.35f, itemY - coinH * 0.35f),
                size = Size(coinW * 0.7f, coinH * 0.7f),
                style = Stroke(width = 2f * scale)
            )
        }
        CollectibleType.GEM -> {
            // Glowing diamond
            val gemSize = 18.dp.toPx() * scale
            val gemPath = Path().apply {
                moveTo(itemX, itemY - gemSize / 2f)
                lineTo(itemX + gemSize / 2f, itemY)
                lineTo(itemX, itemY + gemSize / 2f)
                lineTo(itemX - gemSize / 2f, itemY)
                close()
            }
            drawCircle(
                color = Color(0xFF00F5FF).copy(alpha = 0.4f),
                radius = gemSize * 0.8f,
                center = Offset(itemX, itemY)
            )
            drawPath(path = gemPath, color = Color(0xFF00F5FF))
            drawPath(path = gemPath, color = Color.White, style = Stroke(width = 2f))
        }
        CollectibleType.SHIELD, CollectibleType.MAGNET, CollectibleType.MULTIPLIER, CollectibleType.BOOST -> {
            val pColor = when (item.type) {
                CollectibleType.SHIELD -> Color(0xFF00E676)
                CollectibleType.MAGNET -> Color(0xFFFF007F)
                CollectibleType.MULTIPLIER -> Color(0xFFFFD700)
                else -> Color(0xFF00F5FF)
            }
            val radius = 16.dp.toPx() * scale
            drawCircle(
                color = pColor.copy(alpha = 0.4f),
                radius = radius * 1.3f,
                center = Offset(itemX, itemY)
            )
            drawCircle(
                color = pColor,
                radius = radius,
                center = Offset(itemX, itemY)
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.4f,
                center = Offset(itemX, itemY)
            )
        }
    }
}

private fun DrawScope.drawObstacle(
    obs: Obstacle,
    horizonY: Float,
    groundY: Float,
    topWidth: Float,
    bottomWidth: Float,
    centerX: Float,
    stage: StageType
) {
    val progress = obs.yPos.coerceIn(0f, 1.2f)
    val curTopWidth = topWidth + (bottomWidth - topWidth) * progress
    val curLeft = centerX - curTopWidth / 2f
    val laneW = curTopWidth / 3f
    val obsX = curLeft + laneW * obs.lane + laneW * 0.5f
    val obsY = horizonY + (groundY - horizonY) * progress

    val scale = (0.35f + progress * 0.8f).coerceIn(0.2f, 1.3f)

    when (obs.type) {
        ObstacleType.HIGH_HURDLE -> {
            // Hurdle that needs to be jumped over
            val hurdleW = laneW * 0.85f
            val hurdleH = 26.dp.toPx() * scale
            val topL = Offset(obsX - hurdleW / 2f, obsY - hurdleH)

            // Caution stripes
            drawRoundRect(
                color = Color(0xFFFF2A4B),
                topLeft = topL,
                size = Size(hurdleW, hurdleH),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            // Laser hurdle crossbar
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(obsX - hurdleW / 2f + 4f, obsY - hurdleH / 2f),
                end = Offset(obsX + hurdleW / 2f - 4f, obsY - hurdleH / 2f),
                strokeWidth = 3f * scale
            )
            // Jump indicator icon
            drawCircle(
                color = Color(0xFFFFD700),
                radius = 5f * scale,
                center = Offset(obsX, obsY - hurdleH * 1.3f)
            )
        }
        ObstacleType.LOW_BEAM -> {
            // Low beam / laser grid: runner must slide under
            val beamW = laneW * 0.95f
            val beamH = 14.dp.toPx() * scale
            // Suspended higher off the ground with a gap underneath for sliding!
            val beamY = obsY - 45.dp.toPx() * scale

            // Hanging pillars on sides
            drawLine(
                color = Color(0xFF6B58A0),
                start = Offset(obsX - beamW / 2f, obsY),
                end = Offset(obsX - beamW / 2f, beamY),
                strokeWidth = 4f * scale
            )
            drawLine(
                color = Color(0xFF6B58A0),
                start = Offset(obsX + beamW / 2f, obsY),
                end = Offset(obsX + beamW / 2f, beamY),
                strokeWidth = 4f * scale
            )

            // Glowing Low Barrier Beam
            drawRoundRect(
                color = Color(0xFFFF007F),
                topLeft = Offset(obsX - beamW / 2f, beamY),
                size = Size(beamW, beamH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Slide indicator arrow
            drawLine(
                color = Color.White,
                start = Offset(obsX, obsY - 20.dp.toPx() * scale),
                end = Offset(obsX, obsY - 10.dp.toPx() * scale),
                strokeWidth = 2.5f * scale
            )
        }
        ObstacleType.TALL_BLOCK -> {
            // Solid monolithic obstacle blocking the lane
            val blockW = laneW * 0.8f
            val blockH = 65.dp.toPx() * scale
            drawRoundRect(
                color = Color(0xFF1E1438),
                topLeft = Offset(obsX - blockW / 2f, obsY - blockH),
                size = Size(blockW, blockH),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawRoundRect(
                color = Color(stage.primaryColor),
                topLeft = Offset(obsX - blockW / 2f, obsY - blockH),
                size = Size(blockW, blockH),
                cornerRadius = CornerRadius(8f * scale, 8f * scale),
                style = Stroke(width = 2.5f * scale)
            )
            // Danger X emblem
            drawLine(
                color = Color(0xFFFF2A4B),
                start = Offset(obsX - blockW * 0.3f, obsY - blockH * 0.7f),
                end = Offset(obsX + blockW * 0.3f, obsY - blockH * 0.3f),
                strokeWidth = 3f * scale
            )
            drawLine(
                color = Color(0xFFFF2A4B),
                start = Offset(obsX + blockW * 0.3f, obsY - blockH * 0.7f),
                end = Offset(obsX - blockW * 0.3f, obsY - blockH * 0.3f),
                strokeWidth = 3f * scale
            )
        }
        ObstacleType.ENERGY_SPIKE -> {
            // Energy road spikes
            val spikeW = laneW * 0.75f
            val spikeH = 22.dp.toPx() * scale
            val count = 3
            val step = spikeW / count
            for (i in 0 until count) {
                val sLeft = (obsX - spikeW / 2f) + i * step
                val p = Path().apply {
                    moveTo(sLeft, obsY)
                    lineTo(sLeft + step / 2f, obsY - spikeH)
                    lineTo(sLeft + step, obsY)
                    close()
                }
                drawPath(path = p, color = Color(0xFFFF2A4B))
            }
        }
    }
}

private fun DrawScope.drawRunnerHero(
    engine: GameEngine,
    horizonY: Float,
    groundY: Float,
    topWidth: Float,
    bottomWidth: Float,
    centerX: Float,
    animTime: Float
) {
    val progress = 0.82f // Player line
    val curTopWidth = topWidth + (bottomWidth - topWidth) * progress
    val curLeft = centerX - curTopWidth / 2f
    val laneW = curTopWidth / 3f

    val playerX = curLeft + laneW * engine.playerLane + laneW * 0.5f
    val groundPlayerY = horizonY + (groundY - horizonY) * progress

    // Apply Jump Offset
    val jumpPx = engine.jumpOffset * (groundY - horizonY) * 1.8f
    val playerY = groundPlayerY - jumpPx

    val charColor = Color(engine.currentCharacter?.primaryColorHex ?: 0xFF00F5FF)
    val secondaryColor = Color(engine.currentCharacter?.secondaryColorHex ?: 0xFFFF007F)

    // 1. Shadow on ground
    val shadowScale = (1.0f - (engine.jumpOffset * 2.5f)).coerceIn(0.2f, 1.0f)
    drawOval(
        color = Color(0x77000000),
        topLeft = Offset(playerX - 22.dp.toPx() * shadowScale, groundPlayerY - 6.dp.toPx() * shadowScale),
        size = Size(44.dp.toPx() * shadowScale, 12.dp.toPx() * shadowScale)
    )

    // 2. Motion Trail or Boost trail
    if (engine.activeBoost) {
        for (k in 1..3) {
            val trailY = playerY + k * 14.dp.toPx()
            drawCircle(
                color = Color(0xFF00F5FF).copy(alpha = 0.25f / k),
                radius = (16 - k * 3).dp.toPx(),
                center = Offset(playerX, trailY)
            )
        }
    }

    if (engine.isSliding) {
        // Sliding / Crouch pose
        val slideW = 38.dp.toPx()
        val slideH = 16.dp.toPx()
        drawRoundRect(
            color = charColor,
            topLeft = Offset(playerX - slideW / 2f, playerY - slideH),
            size = Size(slideW, slideH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Jetpack sparks during slide
        drawCircle(
            color = Color(0xFFFFD700),
            radius = 5.dp.toPx(),
            center = Offset(playerX - slideW / 2f, playerY - slideH / 2f)
        )
    } else {
        // Upright Running Pose
        val runCycle = sin(animTime * 14f)

        // Torso
        val torsoH = 26.dp.toPx()
        val torsoW = 18.dp.toPx()
        drawRoundRect(
            color = charColor,
            topLeft = Offset(playerX - torsoW / 2f, playerY - torsoH - 12.dp.toPx()),
            size = Size(torsoW, torsoH),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Head / Helmet
        val headRadius = 9.dp.toPx()
        drawCircle(
            color = secondaryColor,
            radius = headRadius,
            center = Offset(playerX, playerY - torsoH - 18.dp.toPx())
        )
        // Visor
        drawOval(
            color = Color.White,
            topLeft = Offset(playerX - 5.dp.toPx(), playerY - torsoH - 21.dp.toPx()),
            size = Size(10.dp.toPx(), 4.dp.toPx())
        )

        // Legs (Animated run cycle swing)
        val legLength = 14.dp.toPx()
        val leftLegSwing = runCycle * 8.dp.toPx()
        val rightLegSwing = -runCycle * 8.dp.toPx()

        // Left Leg
        drawLine(
            color = charColor,
            start = Offset(playerX - 5.dp.toPx(), playerY - 12.dp.toPx()),
            end = Offset(playerX - 7.dp.toPx(), playerY + leftLegSwing),
            strokeWidth = 5f
        )
        // Right Leg
        drawLine(
            color = charColor,
            start = Offset(playerX + 5.dp.toPx(), playerY - 12.dp.toPx()),
            end = Offset(playerX + 7.dp.toPx(), playerY + rightLegSwing),
            strokeWidth = 5f
        )

        // Glowing boots
        drawCircle(
            color = secondaryColor,
            radius = 4.dp.toPx(),
            center = Offset(playerX - 7.dp.toPx(), playerY + leftLegSwing)
        )
        drawCircle(
            color = secondaryColor,
            radius = 4.dp.toPx(),
            center = Offset(playerX + 7.dp.toPx(), playerY + rightLegSwing)
        )
    }

    // 3. Shield Aura
    if (engine.activeShield) {
        val shieldR = 34.dp.toPx()
        drawCircle(
            color = Color(0xFF00E676).copy(alpha = 0.28f),
            radius = shieldR,
            center = Offset(playerX, playerY - 18.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF00E676),
            radius = shieldR,
            center = Offset(playerX, playerY - 18.dp.toPx()),
            style = Stroke(width = 2.5f)
        )
    }

    // 4. Magnet Aura
    if (engine.activeMagnet) {
        val magR = 36.dp.toPx()
        drawCircle(
            color = Color(0xFFFF007F).copy(alpha = 0.2f),
            radius = magR,
            center = Offset(playerX, playerY - 18.dp.toPx()),
            style = Stroke(width = 2f)
        )
    }
}
