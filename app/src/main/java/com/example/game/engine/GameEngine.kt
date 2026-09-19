package com.example.game.engine

import com.example.audio.SoundManager
import com.example.data.local.entity.CharacterEntity
import com.example.game.model.ActivePowerUp
import com.example.game.model.CollectibleItem
import com.example.game.model.CollectibleType
import com.example.game.model.FloatingText
import com.example.game.model.Obstacle
import com.example.game.model.ObstacleType
import com.example.game.model.Particle
import com.example.game.model.StageType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine(
    private val soundManager: SoundManager
) {
    // Game Loop State
    var isPlaying: Boolean = false
        private set
    var isPaused: Boolean = false
        private set
    var isGameOver: Boolean = false
        private set

    // Stats
    var score: Int = 0
        private set
    var distanceMeters: Int = 0
        private set
    var coinsCollected: Int = 0
        private set
    var gemsCollected: Int = 0
        private set
    var jumpsCount: Int = 0
        private set
    var slidesCount: Int = 0
        private set
    var maxStageIndex: Int = 0
        private set

    // Current Character
    var currentCharacter: CharacterEntity? = null
        private set

    // Player Movement
    var playerLane: Float = 1.0f // Current interpolated lane (0.0 .. 2.0)
        private set
    var targetLane: Int = 1 // 0 = Left, 1 = Center, 2 = Right
        private set
    var jumpOffset: Float = 0.0f // 0.0 = Ground, up to 0.25f
        private set
    private var jumpVelocity: Float = 0.0f
    var isSliding: Boolean = false
        private set
    private var slideTimer: Float = 0.0f

    // Active power-ups & buffs
    var activeShield: Boolean = false
        private set
    var shieldTimer: Float = 0.0f
        private set
    var activeMagnet: Boolean = false
        private set
    var magnetTimer: Float = 0.0f
        private set
    var activeMultiplier: Boolean = false
        private set
    var multiplierTimer: Float = 0.0f
        private set
    var activeBoost: Boolean = false
        private set
    var boostTimer: Float = 0.0f
        private set
    var invincibilityTimer: Float = 0.0f
        private set

    // Stage
    var currentStage: StageType = StageType.CYBER_METRO
        private set
    var stageBannerText: String? = null
        private set
    var stageBannerTimer: Float = 0.0f
        private set

    // World Objects
    val obstacles = mutableListOf<Obstacle>()
    val collectibles = mutableListOf<CollectibleItem>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Speed & Spawning
    private var baseSpeed: Float = 0.38f
    var currentSpeed: Float = 0.38f
        private set
    private var nextSpawnDistance: Float = 0.0f
    private var internalDistanceAcc: Float = 0.0f
    private var nextId: Long = 1L

    fun startGame(character: CharacterEntity) {
        currentCharacter = character
        isPlaying = true
        isPaused = false
        isGameOver = false

        score = 0
        distanceMeters = 0
        coinsCollected = 0
        gemsCollected = 0
        jumpsCount = 0
        slidesCount = 0
        maxStageIndex = 0

        targetLane = 1
        playerLane = 1.0f
        jumpOffset = 0.0f
        jumpVelocity = 0.0f
        isSliding = false
        slideTimer = 0.0f

        obstacles.clear()
        collectibles.clear()
        particles.clear()
        floatingTexts.clear()

        // Apply Character specific initial buffs
        val hasAutoShield = character.id == "titan"
        activeShield = hasAutoShield
        shieldTimer = if (hasAutoShield) 20.0f else 0.0f

        activeMagnet = false
        magnetTimer = 0.0f
        activeMultiplier = false
        multiplierTimer = 0.0f
        activeBoost = false
        boostTimer = 0.0f
        invincibilityTimer = 0.0f

        currentStage = StageType.CYBER_METRO
        stageBannerText = "RUN START: ${currentStage.stageName}"
        stageBannerTimer = 2.5f

        val speedBonus = (character.speedBonusPct / 100f) * 0.05f
        baseSpeed = 0.38f + speedBonus
        currentSpeed = baseSpeed
        nextSpawnDistance = 0.25f
        internalDistanceAcc = 0.0f
    }

    fun pauseGame() {
        if (isPlaying && !isGameOver) {
            isPaused = true
        }
    }

    fun resumeGame() {
        if (isPlaying && !isGameOver) {
            isPaused = false
        }
    }

    fun moveLeft() {
        if (!isPlaying || isPaused || isGameOver) return
        if (targetLane > 0) {
            targetLane--
            soundManager.playButtonClick()
        }
    }

    fun moveRight() {
        if (!isPlaying || isPaused || isGameOver) return
        if (targetLane < 2) {
            targetLane++
            soundManager.playButtonClick()
        }
    }

    fun jump() {
        if (!isPlaying || isPaused || isGameOver) return
        if (jumpOffset <= 0.01f) {
            isSliding = false
            slideTimer = 0.0f
            // Character jump perk
            val boost = if (currentCharacter?.id == "valkyrie") 0.72f else 0.65f
            jumpVelocity = boost
            jumpsCount++
            soundManager.playJumpSound()
        }
    }

    fun slide() {
        if (!isPlaying || isPaused || isGameOver) return
        if (jumpOffset > 0.02f) {
            // Fast fall slam
            jumpVelocity = -0.9f
        }
        isSliding = true
        slideTimer = 0.7f
        slidesCount++
        soundManager.playSlideSound()
    }

    fun update(deltaSeconds: Float) {
        if (!isPlaying || isPaused || isGameOver) return

        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)

        // 1. Update Player position & animations
        updatePlayerMotion(dt)

        // 2. Update Power-ups
        updatePowerUps(dt)

        // 3. Update Speed & Distance
        val speedMult = if (activeBoost) 1.6f else 1.0f
        val actualSpeed = currentSpeed * speedMult
        internalDistanceAcc += actualSpeed * dt * 38f
        val newDistance = internalDistanceAcc.toInt()
        if (newDistance > distanceMeters) {
            val distDiff = newDistance - distanceMeters
            distanceMeters = newDistance
            val charMult = currentCharacter?.scoreMultiplierBonus ?: 1.0f
            val activeMult = if (activeMultiplier) 2.0f else 1.0f
            score += (distDiff * 2 * charMult * activeMult).toInt()
        }

        // Gradually increase base speed with distance (difficulty ramping)
        val speedProgression = (distanceMeters / 1200f) * 0.16f
        currentSpeed = min(0.85f, baseSpeed + speedProgression)

        // 4. Update Stage Progression
        checkStageProgression()

        // 5. Update Banners & Texts
        if (stageBannerTimer > 0f) {
            stageBannerTimer -= dt
            if (stageBannerTimer <= 0f) {
                stageBannerText = null
            }
        }

        // 6. Spawn Obstacles & Collectibles
        handleSpawning(actualSpeed, dt)

        // 7. Update Obstacles
        updateObstacles(actualSpeed, dt)

        // 8. Update Collectibles
        updateCollectibles(actualSpeed, dt)

        // 9. Update Particles & Texts
        updateFX(dt)
    }

    private fun updatePlayerMotion(dt: Float) {
        // Smooth lane interpolation
        val laneDiff = targetLane - playerLane
        playerLane += laneDiff * min(1.0f, dt * 16f)

        // Jump physics (gravity)
        if (jumpOffset > 0.0f || jumpVelocity != 0.0f) {
            jumpOffset += jumpVelocity * dt
            jumpVelocity -= 2.6f * dt // gravity
            if (jumpOffset <= 0.0f) {
                jumpOffset = 0.0f
                jumpVelocity = 0.0f
            }
        }

        // Slide timer
        if (isSliding) {
            slideTimer -= dt
            if (slideTimer <= 0.0f) {
                isSliding = false
            }
        }

        // Invincibility after hit
        if (invincibilityTimer > 0f) {
            invincibilityTimer -= dt
        }
    }

    private fun updatePowerUps(dt: Float) {
        if (activeShield) {
            shieldTimer -= dt
            if (shieldTimer <= 0f) {
                activeShield = false
            }
        }
        if (activeMagnet) {
            magnetTimer -= dt
            if (magnetTimer <= 0f) {
                activeMagnet = false
            }
        }
        if (activeMultiplier) {
            multiplierTimer -= dt
            if (multiplierTimer <= 0f) {
                activeMultiplier = false
            }
        }
        if (activeBoost) {
            boostTimer -= dt
            if (boostTimer <= 0f) {
                activeBoost = false
            }
        }
    }

    private fun checkStageProgression() {
        val nextStage = when {
            distanceMeters >= StageType.QUANTUM_COSMOS.minDistance -> StageType.QUANTUM_COSMOS
            distanceMeters >= StageType.VOLCANO_CAVERN.minDistance -> StageType.VOLCANO_CAVERN
            distanceMeters >= StageType.MYSTIC_JUNGLE.minDistance -> StageType.MYSTIC_JUNGLE
            else -> StageType.CYBER_METRO
        }

        if (nextStage != currentStage) {
            currentStage = nextStage
            maxStageIndex = max(maxStageIndex, nextStage.ordinal)
            stageBannerText = "STAGE ${nextStage.ordinal + 1}: ${nextStage.stageName.uppercase()}!"
            stageBannerTimer = 3.0f
            soundManager.playPowerUpSound()
            // Spawn warp particles
            for (i in 0 until 40) {
                particles.add(
                    Particle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        vx = (Random.nextFloat() - 0.5f) * 1.5f,
                        vy = (Random.nextFloat() - 0.5f) * 1.5f,
                        color = nextStage.accentGlow,
                        life = 1.0f,
                        maxLife = 1.0f,
                        radius = Random.nextFloat() * 8f + 4f
                    )
                )
            }
        }
    }

    private fun handleSpawning(speed: Float, dt: Float) {
        nextSpawnDistance -= speed * dt

        if (nextSpawnDistance <= 0f) {
            // Pick obstacle pattern or collectible cluster
            val spawnRoll = Random.nextFloat()
            val currentLaneChoice = Random.nextInt(3)

            // Select obstacle type according to stage features
            val obstacleType = when (currentStage) {
                StageType.CYBER_METRO -> {
                    when (Random.nextInt(3)) {
                        0 -> ObstacleType.HIGH_HURDLE
                        1 -> ObstacleType.LOW_BEAM
                        else -> ObstacleType.ENERGY_SPIKE
                    }
                }
                StageType.MYSTIC_JUNGLE -> {
                    when (Random.nextInt(4)) {
                        0 -> ObstacleType.HIGH_HURDLE
                        1 -> ObstacleType.LOW_BEAM
                        2 -> ObstacleType.TALL_BLOCK
                        else -> ObstacleType.ENERGY_SPIKE
                    }
                }
                StageType.VOLCANO_CAVERN -> {
                    when (Random.nextInt(4)) {
                        0 -> ObstacleType.HIGH_HURDLE
                        1 -> ObstacleType.LOW_BEAM
                        2 -> ObstacleType.TALL_BLOCK
                        else -> ObstacleType.ENERGY_SPIKE
                    }
                }
                StageType.QUANTUM_COSMOS -> {
                    when (Random.nextInt(4)) {
                        0 -> ObstacleType.HIGH_HURDLE
                        1 -> ObstacleType.LOW_BEAM
                        2 -> ObstacleType.TALL_BLOCK
                        else -> ObstacleType.ENERGY_SPIKE
                    }
                }
            }

            if (spawnRoll < 0.65f) {
                // Spawn Obstacle
                obstacles.add(
                    Obstacle(
                        id = nextId++,
                        lane = currentLaneChoice,
                        yPos = 0.05f,
                        type = obstacleType,
                        stage = currentStage
                    )
                )

                // Sometimes spawn coins in the other lanes
                val otherLane = (currentLaneChoice + 1 + Random.nextInt(2)) % 3
                collectibles.add(
                    CollectibleItem(
                        id = nextId++,
                        lane = otherLane,
                        yPos = 0.05f,
                        type = CollectibleType.COIN
                    )
                )
            } else if (spawnRoll < 0.88f) {
                // Spawn Coin Trail (3 coins in succession)
                val coinLane = Random.nextInt(3)
                for (k in 0..2) {
                    collectibles.add(
                        CollectibleItem(
                            id = nextId++,
                            lane = coinLane,
                            yPos = 0.05f - (k * 0.08f),
                            type = CollectibleType.COIN
                        )
                    )
                }
            } else {
                // Rare Collectible: Gem or Power-up
                val rareLane = Random.nextInt(3)
                val rareType = when (Random.nextInt(6)) {
                    0 -> CollectibleType.GEM
                    1 -> CollectibleType.SHIELD
                    2 -> CollectibleType.MAGNET
                    3 -> CollectibleType.MULTIPLIER
                    4 -> CollectibleType.BOOST
                    else -> CollectibleType.GEM
                }
                collectibles.add(
                    CollectibleItem(
                        id = nextId++,
                        lane = rareLane,
                        yPos = 0.05f,
                        type = rareType
                    )
                )
            }

            // Next spawn spacing scales down with distance to ramp intensity
            val minSpacing = max(0.28f, 0.48f - (distanceMeters / 1500f) * 0.14f)
            nextSpawnDistance = minSpacing + Random.nextFloat() * 0.15f
        }
    }

    private fun updateObstacles(speed: Float, dt: Float) {
        val iterator = obstacles.iterator()
        val playerHitboxYMin = 0.76f
        val playerHitboxYMax = 0.88f

        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.yPos += speed * dt

            // Collision check with player
            if (!obs.isPassed && obs.yPos in playerHitboxYMin..playerHitboxYMax) {
                val laneDistance = abs(playerLane - obs.lane.toFloat())
                if (laneDistance < 0.55f) {
                    // Lane matches! Check vertical avoidance (Jump or Slide)
                    var isAvoided = false
                    when (obs.type) {
                        ObstacleType.HIGH_HURDLE -> {
                            if (jumpOffset > 0.07f) {
                                isAvoided = true // Jumped successfully over hurdle!
                            }
                        }
                        ObstacleType.LOW_BEAM -> {
                            if (isSliding) {
                                isAvoided = true // Slid successfully under low beam!
                            }
                        }
                        ObstacleType.TALL_BLOCK, ObstacleType.ENERGY_SPIKE -> {
                            // Cannot jump or slide, must dodge
                            isAvoided = false
                        }
                    }

                    if (!isAvoided) {
                        // Impact!
                        if (activeBoost) {
                            // Smashes through obstacle!
                            obs.isPassed = true
                            createShatterFX(obs.lane, obs.yPos, currentStage.primaryColor)
                            soundManager.playCrashSound()
                        } else if (activeShield) {
                            // Shield absorbs hit
                            activeShield = false
                            shieldTimer = 0f
                            invincibilityTimer = 1.2f
                            obs.isPassed = true
                            createShatterFX(obs.lane, obs.yPos, 0xFF00E676)
                            addFloatingText(obs.lane, "SHIELD BROKEN!", 0xFF00E676)
                            soundManager.playCrashSound()
                        } else if (invincibilityTimer > 0f) {
                            // Grace period
                            obs.isPassed = true
                        } else {
                            // Game Over
                            triggerGameOver()
                            return
                        }
                    } else {
                        obs.isPassed = true
                        // Rewarding bonus score for skillful dodge!
                        score += 30
                    }
                }
            }

            // Clean up off-screen
            if (obs.yPos > 1.15f) {
                iterator.remove()
            }
        }
    }

    private fun updateCollectibles(speed: Float, dt: Float) {
        val iterator = collectibles.iterator()
        val playerHitboxYMin = 0.74f
        val playerHitboxYMax = 0.90f

        val hasMagnetPerk = activeMagnet || ((currentCharacter?.magnetBonusPct ?: 0) > 20)
        val magnetRadius = if (activeMagnet) 0.6f else 0.35f

        while (iterator.hasNext()) {
            val item = iterator.next()
            item.yPos += speed * dt

            // Magnet Attraction
            if (hasMagnetPerk && (item.type == CollectibleType.COIN || item.type == CollectibleType.GEM)) {
                val dy = abs(item.yPos - 0.82f)
                val dx = abs(item.lane.toFloat() - playerLane)
                if (dy < magnetRadius) {
                    val laneDir = (playerLane - item.lane.toFloat())
                    item.lane = if (laneDir > 0.3f) item.lane + 1 else if (laneDir < -0.3f) item.lane - 1 else item.lane
                    item.yPos += (0.82f - item.yPos) * min(1.0f, dt * 10f)
                }
            }

            // Pickup check
            if (!item.isCollected && item.yPos in playerHitboxYMin..playerHitboxYMax) {
                val laneDistance = abs(playerLane - item.lane.toFloat())
                if (laneDistance < 0.6f) {
                    item.isCollected = true
                    collectItem(item)
                    iterator.remove()
                    continue
                }
            }

            if (item.yPos > 1.15f) {
                iterator.remove()
            }
        }
    }

    private fun collectItem(item: CollectibleItem) {
        when (item.type) {
            CollectibleType.COIN -> {
                coinsCollected++
                val mult = if (activeMultiplier) 2 else 1
                val bonus = (15 * mult * (currentCharacter?.scoreMultiplierBonus ?: 1.0f)).toInt()
                score += bonus
                soundManager.playCoinSound()
                addFloatingText(item.lane, "+$bonus", 0xFFFFD700)
                createSparkleFX(item.lane, item.yPos, 0xFFFFD700, 6)
            }
            CollectibleType.GEM -> {
                gemsCollected++
                score += 150
                soundManager.playGemSound()
                addFloatingText(item.lane, "+1 GEM!", 0xFF00F5FF)
                createSparkleFX(item.lane, item.yPos, 0xFF00F5FF, 12)
            }
            CollectibleType.SHIELD -> {
                val bonusSec = (currentCharacter?.shieldBonusPct ?: 0) * 0.1f
                activeShield = true
                shieldTimer = 10.0f + bonusSec
                soundManager.playPowerUpSound()
                addFloatingText(item.lane, "ENERGY SHIELD!", 0xFF00E676)
                createSparkleFX(item.lane, item.yPos, 0xFF00E676, 15)
            }
            CollectibleType.MAGNET -> {
                val bonusSec = (currentCharacter?.magnetBonusPct ?: 0) * 0.08f
                activeMagnet = true
                magnetTimer = 10.0f + bonusSec
                soundManager.playPowerUpSound()
                addFloatingText(item.lane, "COIN MAGNET!", 0xFFFF007F)
                createSparkleFX(item.lane, item.yPos, 0xFFFF007F, 15)
            }
            CollectibleType.MULTIPLIER -> {
                val bonusSec = if (currentCharacter?.id == "shadow") 5.0f else 0.0f
                activeMultiplier = true
                multiplierTimer = 10.0f + bonusSec
                soundManager.playPowerUpSound()
                addFloatingText(item.lane, "2X SCORE!", 0xFFFFD700)
                createSparkleFX(item.lane, item.yPos, 0xFFFFD700, 15)
            }
            CollectibleType.BOOST -> {
                activeBoost = true
                boostTimer = 5.0f
                invincibilityTimer = 5.5f
                soundManager.playPowerUpSound()
                addFloatingText(item.lane, "HYPER BOOST!", 0xFF00F5FF)
                createSparkleFX(item.lane, item.yPos, 0xFF00F5FF, 20)
            }
        }
    }

    private fun triggerGameOver() {
        isGameOver = true
        isPlaying = false
        soundManager.playCrashSound()
        createShatterFX(targetLane, 0.82f, 0xFFFF2A4B, count = 35)
    }

    private fun createShatterFX(lane: Int, yPos: Float, color: Long, count: Int = 18) {
        val normX = 0.25f + lane * 0.25f
        for (i in 0 until count) {
            particles.add(
                Particle(
                    x = normX,
                    y = yPos,
                    vx = (Random.nextFloat() - 0.5f) * 0.8f,
                    vy = (Random.nextFloat() - 0.5f) * 0.8f,
                    color = color,
                    life = 0.7f,
                    maxLife = 0.7f,
                    radius = Random.nextFloat() * 6f + 3f
                )
            )
        }
    }

    private fun createSparkleFX(lane: Int, yPos: Float, color: Long, count: Int = 8) {
        val normX = 0.25f + lane * 0.25f
        for (i in 0 until count) {
            particles.add(
                Particle(
                    x = normX,
                    y = yPos,
                    vx = (Random.nextFloat() - 0.5f) * 0.4f,
                    vy = -Random.nextFloat() * 0.5f,
                    color = color,
                    life = 0.5f,
                    maxLife = 0.5f,
                    radius = Random.nextFloat() * 4f + 2f
                )
            )
        }
    }

    private fun addFloatingText(lane: Int, text: String, color: Long) {
        val normX = 0.25f + lane * 0.25f
        floatingTexts.add(
            FloatingText(
                x = normX,
                y = 0.78f,
                text = text,
                color = color,
                life = 0.9f
            )
        )
    }

    private fun updateFX(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            if (p.life <= 0f) {
                pIter.remove()
            }
        }

        val fIter = floatingTexts.iterator()
        while (fIter.hasNext()) {
            val f = fIter.next()
            f.y -= 0.18f * dt // float upward
            f.life -= dt
            if (f.life <= 0f) {
                fIter.remove()
            }
        }
    }

    fun getActivePowerUps(): List<ActivePowerUp> {
        val list = mutableListOf<ActivePowerUp>()
        if (activeShield) list.add(ActivePowerUp(CollectibleType.SHIELD, shieldTimer, 10f))
        if (activeMagnet) list.add(ActivePowerUp(CollectibleType.MAGNET, magnetTimer, 10f))
        if (activeMultiplier) list.add(ActivePowerUp(CollectibleType.MULTIPLIER, multiplierTimer, 10f))
        if (activeBoost) list.add(ActivePowerUp(CollectibleType.BOOST, boostTimer, 5f))
        return list
    }
}
