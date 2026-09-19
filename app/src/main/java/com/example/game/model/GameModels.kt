package com.example.game.model

enum class StageType(
    val stageName: String,
    val subtitle: String,
    val minDistance: Int,
    val maxDistance: Int,
    val primaryColor: Long,
    val secondaryColor: Long,
    val trackColor: Long,
    val accentGlow: Long
) {
    CYBER_METRO(
        stageName = "Cyber Metro",
        subtitle = "Sector 01 - Neon Grid",
        minDistance = 0,
        maxDistance = 400,
        primaryColor = 0xFF00F5FF,
        secondaryColor = 0xFFFF007F,
        trackColor = 0xFF140D2B,
        accentGlow = 0xFF00F5FF
    ),
    MYSTIC_JUNGLE(
        stageName = "Mystic Jungle",
        subtitle = "Sector 02 - Ancient Ruins",
        minDistance = 400,
        maxDistance = 900,
        primaryColor = 0xFF00E676,
        secondaryColor = 0xFFFFD700,
        trackColor = 0xFF0D2418,
        accentGlow = 0xFF69F0AE
    ),
    VOLCANO_CAVERN(
        stageName = "Volcano Cavern",
        subtitle = "Sector 03 - Magma Core",
        minDistance = 900,
        maxDistance = 1500,
        primaryColor = 0xFFFF5722,
        secondaryColor = 0xFFFFAB00,
        trackColor = 0xFF2A0D08,
        accentGlow = 0xFFFF3D00
    ),
    QUANTUM_COSMOS(
        stageName = "Quantum Cosmos",
        subtitle = "Sector 04 - Warp Velocity",
        minDistance = 1500,
        maxDistance = Int.MAX_VALUE,
        primaryColor = 0xFFBD00FF,
        secondaryColor = 0xFF00E5FF,
        trackColor = 0xFF0B0620,
        accentGlow = 0xFFE040FB
    )
}

enum class ObstacleType {
    HIGH_HURDLE, // Jump over
    LOW_BEAM,    // Slide under
    TALL_BLOCK,  // Dodge / switch lane
    ENERGY_SPIKE // Dodge / switch lane
}

enum class CollectibleType {
    COIN,
    GEM,
    SHIELD,
    MAGNET,
    MULTIPLIER,
    BOOST
}

data class Obstacle(
    val id: Long,
    var lane: Int, // 0 = Left, 1 = Center, 2 = Right
    var yPos: Float, // Normalized 0.0 (horizon) to 1.0 (player line)
    val type: ObstacleType,
    val stage: StageType,
    var isPassed: Boolean = false
)

data class CollectibleItem(
    val id: Long,
    var lane: Int,
    var yPos: Float,
    val type: CollectibleType,
    var isCollected: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    var life: Float,
    val maxLife: Float,
    val radius: Float
)

data class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Long,
    var life: Float,
    val maxLife: Float = 1.0f
)

data class ActivePowerUp(
    val type: CollectibleType,
    var remainingSeconds: Float,
    val maxSeconds: Float
)
