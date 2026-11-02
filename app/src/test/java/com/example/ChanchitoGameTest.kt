package com.example

import com.example.game.audio.GameAudioEngine
import com.example.game.engine.GameEngine
import com.example.game.levels.LevelRepository
import com.example.game.model.EnemyType
import com.example.game.model.ThrowableType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChanchitoGameTest {

    private lateinit var audioEngine: GameAudioEngine
    private lateinit var gameEngine: GameEngine

    @Before
    fun setUp() {
        audioEngine = GameAudioEngine()
        gameEngine = GameEngine(audioEngine = audioEngine, vibrator = null)
    }

    @Test
    fun testLevel1Initialization() {
        gameEngine.loadLevel(1)
        val level = gameEngine.currentLevel
        assertEquals(1, level.id)
        assertTrue("Level 1 should have platforms", level.platforms.isNotEmpty())
        assertTrue("Level 1 should have enemies", level.enemies.isNotEmpty())
        assertTrue("Level 1 should have collectibles", level.collectibles.isNotEmpty())
        assertEquals(3, gameEngine.player.health)
        assertEquals(5, gameEngine.player.ammo[ThrowableType.APPLE])
    }

    @Test
    fun testJumpAndDoubleJumpMechanics() {
        gameEngine.loadLevel(1)
        gameEngine.player.isGrounded = true

        // First jump
        gameEngine.onJumpPressed()
        assertTrue("Player should have upward velocity on jump", gameEngine.player.vy < 0f)
        assertFalse("Player should not be grounded", gameEngine.player.isGrounded)

        // Mid-air double jump
        gameEngine.onJumpPressed()
        assertTrue("Player should have performed double jump", gameEngine.player.didDoubleJump)
    }

    @Test
    fun testBellySlamActivation() {
        gameEngine.loadLevel(1)
        gameEngine.player.isGrounded = false
        gameEngine.onDownPressed()

        assertTrue("Player should be belly slamming", gameEngine.player.isBellySlamming)
        assertTrue("Player downward velocity should be high", gameEngine.player.vy >= 800f)
    }

    @Test
    fun testEnemyStompAndDizzyBirds() {
        gameEngine.loadLevel(1)
        val enemy = gameEngine.currentLevel.enemies.first { it.type == EnemyType.CHIBI_KNIFE }

        // Position player directly above the enemy moving downward
        gameEngine.player.x = enemy.x
        gameEngine.player.y = enemy.y - gameEngine.player.height + 5f
        gameEngine.player.vy = 200f // moving down

        // Run one physics update frame
        gameEngine.update(0.016f)

        // Verify player bounced high
        assertTrue("Player should bounce upward after stomping enemy", gameEngine.player.vy < 0f)
        // Verify enemy is stunned with dizzy birds
        assertTrue("Enemy should be stunned", enemy.isStunned)
        assertTrue("Enemy should have dizzy birds orbiting", enemy.dizzyBirds.isNotEmpty())
        assertTrue("Enemy stunned timer should be active", enemy.stunnedTimer > 0f)
    }

    @Test
    fun testThrowingProjectiles() {
        gameEngine.loadLevel(1)
        val initialAmmo = gameEngine.player.ammo[ThrowableType.APPLE] ?: 0

        gameEngine.onThrowPressed()

        assertEquals("Ammo should decrease by 1", initialAmmo - 1, gameEngine.player.ammo[ThrowableType.APPLE])
        assertEquals("One projectile should be spawned", 1, gameEngine.projectiles.size)
    }

    @Test
    fun testBossLevelSetup() {
        gameEngine.loadLevel(4)
        val boss = gameEngine.currentLevel.boss
        assertNotNull("Level 4 must have Don Ramiro boss", boss)
        assertEquals("Don Ramiro", boss?.name)
        assertEquals(12, boss?.maxHealth)
        assertEquals(12, boss?.health)
    }
}
