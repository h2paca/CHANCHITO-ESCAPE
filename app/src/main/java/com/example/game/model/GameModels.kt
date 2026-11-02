package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class Season(
    val displayName: String,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val hillColor: Color,
    val grassColor: Color,
    val dirtColor: Color,
    val description: String
) {
    SPRING(
        displayName = "Primavera en el Corral",
        skyColorTop = Color(0xFF64B5F6),
        skyColorBottom = Color(0xFFE1F5FE),
        hillColor = Color(0xFF81C784),
        grassColor = Color(0xFF4CAF50),
        dirtColor = Color(0xFF795548),
        description = "Flores de cerezo, barro fresco y el primer escape."
    ),
    SUMMER(
        displayName = "Verano en el Maizal",
        skyColorTop = Color(0xFF29B6F6),
        skyColorBottom = Color(0xFFFFF9C4),
        hillColor = Color(0xFFFFD54F),
        grassColor = Color(0xFF8BC34A),
        dirtColor = Color(0xFF8D6E63),
        description = "Campos dorados de maíz, calor sofocante y fardos de heno."
    ),
    AUTUMN(
        displayName = "Otoño en la Huerta",
        skyColorTop = Color(0xFFFFB74D),
        skyColorBottom = Color(0xFFFFE0B2),
        hillColor = Color(0xFFFF8A65),
        grassColor = Color(0xFFD87D39),
        dirtColor = Color(0xFF5D4037),
        description = "Hojas secas, calabazas gigantes y charcos resbalosos."
    ),
    WINTER(
        displayName = "Invierno: El Gran Granero",
        skyColorTop = Color(0xFF37474F),
        skyColorBottom = Color(0xFF90A4AE),
        hillColor = Color(0xFFCFD8DC),
        grassColor = Color(0xFFECEFF1),
        dirtColor = Color(0xFF455A64),
        description = "Ventisca gélida y la gran batalla contra Don Ramiro."
    )
}

enum class ThrowableType(
    val displayName: String,
    val emoji: String,
    val damage: Int,
    val tint: Color,
    val initialSpeed: Float
) {
    APPLE(
        displayName = "Manzana Podrida",
        emoji = "🍎",
        damage = 1,
        tint = Color(0xFFE53935),
        initialSpeed = 480f
    ),
    CORN(
        displayName = "Mazorca de Maíz",
        emoji = "🌽",
        damage = 2,
        tint = Color(0xFFFFC107),
        initialSpeed = 620f
    ),
    MUD(
        displayName = "Bola de Lodo",
        emoji = "🟤",
        damage = 1,
        tint = Color(0xFF6D4C41),
        initialSpeed = 440f
    ),
    ACORN(
        displayName = "Bellota Dorada",
        emoji = "🌰",
        damage = 3,
        tint = Color(0xFFFFD700),
        initialSpeed = 540f
    )
}

enum class EnemyType {
    CHIBI_KNIFE,   // Chibi farm kid with knife
    CHIBI_FORK,    // Chibi farm kid with wooden fork
    ROOSTER,       // Jumping rooster
    CHICKEN        // Rolling baby hen
}

data class DizzyBird(
    var angle: Float,
    val distance: Float = 22f,
    val verticalOffset: Float = -24f,
    val speed: Float = 5.5f,
    var wingPhase: Float = 0f
)

data class Enemy(
    val id: String,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 36f,
    val height: Float = 44f,
    var facingRight: Boolean = false,
    var isGrounded: Boolean = false,
    var health: Int = 2,
    var isStunned: Boolean = false,
    var stunnedTimer: Float = 0f,
    var dizzyBirds: List<DizzyBird> = listOf(
        DizzyBird(angle = 0f),
        DizzyBird(angle = 2.094f), // 120 deg
        DizzyBird(angle = 4.188f)  // 240 deg
    ),
    var isDefeated: Boolean = false,
    var defeatedTimer: Float = 0f,
    val patrolMinX: Float,
    val patrolMaxX: Float,
    val baseSpeed: Float = 110f,
    var animTime: Float = 0f
)

enum class BossState {
    INTRO,
    IDLE,
    PITCHFORK_PREPARE,
    PITCHFORK_THROW,
    HAY_ROLL_PREPARE,
    HAY_ROLL,
    GROUND_STOMP_JUMP,
    GROUND_STOMP_SLAM,
    CHICKEN_CALL,
    HURT,
    STUNNED,
    DEFEATED
}

data class Boss(
    val name: String = "Don Ramiro",
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 72f,
    val height: Float = 92f,
    var health: Int = 12,
    val maxHealth: Int = 12,
    var phase: Int = 1, // 1: Normal, 2: Enraged (Red face, faster)
    var state: BossState = BossState.INTRO,
    var stateTimer: Float = 0f,
    var attackCooldown: Float = 2.5f,
    var facingRight: Boolean = false,
    var isInvulnerable: Boolean = false,
    var invulnerableTimer: Float = 0f,
    var animTime: Float = 0f
)

data class PigPlayer(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 38f,
    val height: Float = 36f,
    var facingRight: Boolean = true,
    var isGrounded: Boolean = false,
    var isRunning: Boolean = false,
    var isDucking: Boolean = false,
    var isBellySlamming: Boolean = false,
    var canDoubleJump: Boolean = true,
    var didDoubleJump: Boolean = false,
    var health: Int = 3,
    val maxHealth: Int = 3,
    var stamina: Float = 100f,
    val maxStamina: Float = 100f,
    var currentThrowable: ThrowableType = ThrowableType.APPLE,
    var ammo: MutableMap<ThrowableType, Int> = mutableMapOf(
        ThrowableType.APPLE to 5,
        ThrowableType.CORN to 0,
        ThrowableType.MUD to 0,
        ThrowableType.ACORN to 0
    ),
    var score: Int = 0,
    var acorns: Int = 0,
    var invulnerableTimer: Float = 0f,
    var slamShockwaveRadius: Float = 0f,
    var flipAngle: Float = 0f,
    var animTime: Float = 0f
)

enum class PlatformType {
    SOLID_GROUND,
    WOOD_PLANK,
    ONE_WAY,
    BREAKABLE_CRATE,
    BOUNCY_HAY,
    SLIPPERY_MUD,
    LEVER_TRAP
}

data class Platform(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType = PlatformType.SOLID_GROUND,
    var isBroken: Boolean = false,
    var leverActive: Boolean = false,
    var triggerTimer: Float = 0f
)

enum class CollectibleType {
    APPLE,
    CORN,
    MUD,
    ACORN,
    GOLD_COIN,
    HEART
}

data class Collectible(
    val id: String,
    val type: CollectibleType,
    val x: Float,
    val y: Float,
    var collected: Boolean = false,
    var bobOffset: Float = 0f,
    val width: Float = 26f,
    val height: Float = 26f
)

enum class ProjectileSource {
    PLAYER,
    BOSS_PITCHFORK,
    BOSS_HAYBALE,
    BOSS_CHICKEN
}

data class Projectile(
    val id: String,
    val type: ThrowableType?,
    val source: ProjectileSource,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 10f,
    var rotation: Float = 0f,
    var active: Boolean = true,
    var bouncesLeft: Int = 1,
    var lifeTimer: Float = 5f
)

enum class ParticleType {
    DUST,
    SAKURA_PETAL,
    AUTUMN_LEAF,
    SNOWFLAKE,
    STAR_SPARKLE,
    DIZZY_BIRD_TRAIL,
    MUD_SPLAT,
    APPLE_CHUNK,
    POPCORN_BURST,
    SHOCKWAVE_RING
}

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var radius: Float,
    var alpha: Float = 1f,
    var life: Float,
    val maxLife: Float,
    val type: ParticleType,
    var rotation: Float = 0f,
    var rotSpeed: Float = 0f
)

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1f,
    var life: Float = 1.0f
)

data class GameLevel(
    val id: Int,
    val season: Season,
    val title: String,
    val subtitle: String,
    val worldWidth: Float,
    val worldHeight: Float = 900f,
    val playerStartX: Float = 80f,
    val playerStartY: Float = 600f,
    val exitX: Float,
    val exitY: Float,
    val platforms: List<Platform>,
    val enemies: List<Enemy>,
    val collectibles: List<Collectible>,
    val boss: Boss? = null,
    val hints: List<Pair<Float, String>> = emptyList()
)
