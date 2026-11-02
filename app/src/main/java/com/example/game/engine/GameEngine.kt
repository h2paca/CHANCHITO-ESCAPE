package com.example.game.engine

import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import com.example.game.audio.GameAudioEngine
import com.example.game.levels.LevelRepository
import com.example.game.model.*
import kotlin.math.*

class GameEngine(
    val audioEngine: GameAudioEngine,
    val vibrator: Vibrator? = null
) {
    var currentLevel: GameLevel = LevelRepository.getLevel(1)
        private set

    var player: PigPlayer = PigPlayer(
        x = currentLevel.playerStartX,
        y = currentLevel.playerStartY
    )
        private set

    val particles = mutableListOf<GameParticle>()
    val projectiles = mutableListOf<Projectile>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Camera
    var cameraX = 0f
        private set
    var cameraY = 0f
        private set
    var screenShakeTimer = 0f
    var screenShakeIntensity = 0f

    // Game state
    var isLevelCompleted = false
    var isGameOver = false
    var isGameWon = false // Finished all 4 levels including Boss
    var gameTime = 0f

    // Input state
    var inputLeft = false
    var inputRight = false
    var inputDown = false
    var inputRun = false
    var isJumpPressed = false

    // Constants
    private val gravity = 1350f
    private val normalWalkSpeed = 230f
    private val sprintSpeed = 380f
    private val jumpVelocity = -520f
    private val doubleJumpVelocity = -480f
    private val bellySlamSpeed = 820f

    init {
        loadLevel(1)
    }

    fun loadLevel(levelId: Int) {
        val level = LevelRepository.getLevel(levelId)
        currentLevel = level
        player = PigPlayer(
            x = level.playerStartX,
            y = level.playerStartY,
            health = 3,
            stamina = 100f
        )
        // Set initial ammo
        player.ammo[ThrowableType.APPLE] = 5
        player.ammo[ThrowableType.CORN] = if (levelId >= 2) 3 else 0
        player.ammo[ThrowableType.MUD] = if (levelId >= 3) 3 else 0
        player.ammo[ThrowableType.ACORN] = if (levelId >= 2) 2 else 0

        projectiles.clear()
        particles.clear()
        floatingTexts.clear()
        isLevelCompleted = false
        isGameOver = false
        isGameWon = false
        cameraX = 0f
        cameraY = 0f

        // Set audio theme
        if (level.boss != null) {
            audioEngine.currentTheme = GameAudioEngine.BgmTheme.BOSS_RAMIRO
            audioEngine.playBossRoar()
        } else {
            audioEngine.currentTheme = GameAudioEngine.BgmTheme.PEACEFUL_FARM
        }
    }

    fun nextLevel() {
        val nextId = currentLevel.id + 1
        if (nextId <= LevelRepository.totalLevels) {
            loadLevel(nextId)
        } else {
            isGameWon = true
            audioEngine.playVictory()
        }
    }

    fun restartCurrentLevel() {
        loadLevel(currentLevel.id)
    }

    // --- Inputs ---

    fun onJumpPressed() {
        if (isGameOver || isLevelCompleted) return
        isJumpPressed = true

        if (player.isGrounded) {
            player.vy = jumpVelocity
            player.isGrounded = false
            player.canDoubleJump = true
            player.didDoubleJump = false
            audioEngine.playJump()
            vibrate(25)
            spawnJumpDust(player.x + player.width / 2, player.y + player.height)
        } else if (player.canDoubleJump && !player.didDoubleJump) {
            // Innovative Double Jump with sparkle flip
            player.vy = doubleJumpVelocity
            player.didDoubleJump = true
            player.canDoubleJump = false
            audioEngine.playDoubleJump()
            vibrate(35)
            spawnDoubleJumpSparkles(player.x + player.width / 2, player.y + player.height / 2)
        } else if (!player.isGrounded && inputDown) {
            // Mid-air Salto Bomba (Belly Flop)
            triggerBellySlam()
        }
    }

    fun onJumpReleased() {
        isJumpPressed = false
        // Variable jump height tap mechanic
        if (player.vy < -180f && !player.isBellySlamming) {
            player.vy = -180f
        }
    }

    fun onDownPressed() {
        inputDown = true
        if (!player.isGrounded) {
            triggerBellySlam()
        }
    }

    fun onDownReleased() {
        inputDown = false
    }

    private fun triggerBellySlam() {
        if (!player.isBellySlamming) {
            player.isBellySlamming = true
            player.vy = bellySlamSpeed
            player.vx = 0f
            audioEngine.playOink()
            vibrate(40)
        }
    }

    fun onThrowPressed() {
        if (isGameOver || isLevelCompleted) return

        // Check if player has ammo of current selected type
        val currentType = player.currentThrowable
        val count = player.ammo[currentType] ?: 0

        val typeToThrow = if (count > 0) {
            player.ammo[currentType] = count - 1
            currentType
        } else {
            // Look for any available ammo
            val available = player.ammo.entries.firstOrNull { it.value > 0 }?.key
            if (available != null) {
                player.currentThrowable = available
                player.ammo[available] = (player.ammo[available] ?: 1) - 1
                available
            } else {
                null
            }
        }

        if (typeToThrow != null) {
            throwProjectile(typeToThrow)
        } else {
            // Empty ammo click sound
            audioEngine.playOink()
            addFloatingText("¡Sin munición!", player.x, player.y - 20f, Color.White)
        }
    }

    private fun throwProjectile(type: ThrowableType) {
        val dir = if (player.facingRight) 1f else -1f
        val spawnX = if (player.facingRight) player.x + player.width + 4f else player.x - 14f
        val spawnY = player.y + player.height / 2 - 6f

        val (vx, vy) = when (type) {
            ThrowableType.APPLE -> Pair(dir * type.initialSpeed, -180f)
            ThrowableType.CORN -> Pair(dir * type.initialSpeed, -60f)
            ThrowableType.MUD -> Pair(dir * type.initialSpeed, -240f)
            ThrowableType.ACORN -> Pair(dir * type.initialSpeed, -120f)
        }

        projectiles.add(
            Projectile(
                id = "proj_${System.nanoTime()}",
                type = type,
                source = ProjectileSource.PLAYER,
                x = spawnX,
                y = spawnY,
                vx = vx,
                vy = vy,
                radius = 12f
            )
        )
        audioEngine.playThrow()
        vibrate(20)
    }

    fun switchThrowable() {
        val types = ThrowableType.values()
        val nextIndex = (player.currentThrowable.ordinal + 1) % types.size
        player.currentThrowable = types[nextIndex]
        audioEngine.playPickup()
    }

    // --- Main Physics & Simulation Loop ---

    fun update(dt: Float) {
        if (isGameOver) return
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        gameTime += clampedDt

        updatePlayer(clampedDt)
        updatePlatforms(clampedDt)
        updateEnemies(clampedDt)
        updateBoss(clampedDt)
        updateProjectiles(clampedDt)
        updateCollectibles(clampedDt)
        updateAmbientParticles(clampedDt)
        updateParticles(clampedDt)
        updateFloatingTexts(clampedDt)
        updateCamera(clampedDt)
        checkLevelExit()
    }

    private fun updatePlayer(dt: Float) {
        // Stamina and running
        val wantsRun = inputRun && player.stamina > 5f && (inputLeft || inputRight)
        if (wantsRun) {
            player.isRunning = true
            player.stamina = (player.stamina - 28f * dt).coerceAtLeast(0f)
        } else {
            player.isRunning = false
            player.stamina = (player.stamina + 20f * dt).coerceAtMost(player.maxStamina)
        }

        // Horizontal speed
        val targetSpeed = if (player.isRunning) sprintSpeed else normalWalkSpeed
        if (inputLeft && !player.isBellySlamming) {
            player.vx = -targetSpeed
            player.facingRight = false
        } else if (inputRight && !player.isBellySlamming) {
            player.vx = targetSpeed
            player.facingRight = true
        } else if (player.isGrounded) {
            player.vx *= 0.65f // ground friction
        } else {
            player.vx *= 0.94f // air drag
        }

        // Apply gravity if not grounded
        if (!player.isGrounded) {
            val effGravity = if (player.isBellySlamming) gravity * 2.2f else gravity
            player.vy += effGravity * dt
        }

        // Running dust particles
        if (player.isGrounded && abs(player.vx) > 160f && Math.random() < 0.45) {
            spawnRunDust(player.x + (if (player.facingRight) 4f else player.width - 4f), player.y + player.height)
        }

        // Move horizontally and check collisions
        player.x += player.vx * dt
        checkPlayerHorizontalCollisions()

        // Move vertically and check collisions
        player.y += player.vy * dt
        checkPlayerVerticalCollisions()

        // Double jump flip animation
        if (player.didDoubleJump && !player.isGrounded) {
            player.flipAngle += (if (player.facingRight) 720f else -720f) * dt
        } else {
            player.flipAngle = 0f
        }

        // Invulnerability timer
        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer -= dt
        }

        // Fall into pit
        if (player.y > currentLevel.worldHeight + 100f) {
            damagePlayer(3)
        }

        player.animTime += dt
    }

    private fun checkPlayerHorizontalCollisions() {
        for (platform in currentLevel.platforms) {
            if (platform.isBroken) continue
            if (platform.type == PlatformType.ONE_WAY) continue

            if (boxOverlap(
                    player.x, player.y, player.width, player.height,
                    platform.x, platform.y, platform.width, platform.height
                )
            ) {
                if (player.vx > 0f) {
                    player.x = platform.x - player.width
                    player.vx = 0f
                } else if (player.vx < 0f) {
                    player.x = platform.x + platform.width
                    player.vx = 0f
                }
            }
        }

        // Clamp to level bounds
        if (player.x < 0f) {
            player.x = 0f
            player.vx = 0f
        }
        if (player.x + player.width > currentLevel.worldWidth) {
            player.x = currentLevel.worldWidth - player.width
            player.vx = 0f
        }
    }

    private fun checkPlayerVerticalCollisions() {
        var landed = false

        for (platform in currentLevel.platforms) {
            if (platform.isBroken) continue

            val overlapsX = (player.x + player.width > platform.x + 4f) &&
                    (player.x < platform.x + platform.width - 4f)

            if (!overlapsX) continue

            // Falling downwards onto platform top
            if (player.vy >= 0f) {
                val playerPrevBottom = player.y + player.height - (player.vy * 0.05f)
                val isAboveTop = playerPrevBottom <= platform.y + 16f
                val isInsidePlatformTop = (player.y + player.height >= platform.y) &&
                        (player.y + player.height <= platform.y + platform.height + 14f)

                if (isAboveTop && isInsidePlatformTop) {
                    player.y = platform.y - player.height
                    player.isGrounded = true
                    landed = true

                    // Innovative Belly Slam Impact
                    if (player.isBellySlamming) {
                        onBellySlamLand(platform)
                    }

                    // Special Platform Interactions
                    when (platform.type) {
                        PlatformType.BOUNCY_HAY -> {
                            player.vy = -750f // super bouncy trampoline
                            player.isGrounded = false
                            landed = false
                            player.canDoubleJump = true
                            player.didDoubleJump = false
                            audioEngine.playJump()
                            vibrate(30)
                            spawnShockwave(platform.x + platform.width / 2, platform.y, Color(0xFFFFD54F))
                        }
                        PlatformType.BREAKABLE_CRATE -> {
                            if (player.isBellySlamming) {
                                breakCrate(platform)
                            } else {
                                player.vy = 0f
                            }
                        }
                        PlatformType.LEVER_TRAP -> {
                            activateTrapLever(platform)
                            player.vy = 0f
                        }
                        else -> {
                            player.vy = 0f
                        }
                    }

                    player.canDoubleJump = true
                    player.didDoubleJump = false
                    player.isBellySlamming = false
                    break
                }
            } else if (player.vy < 0f && platform.type != PlatformType.ONE_WAY) {
                // Hitting head from below
                if (player.y <= platform.y + platform.height &&
                    player.y >= platform.y
                ) {
                    player.y = platform.y + platform.height
                    player.vy = 0f
                    if (platform.type == PlatformType.BREAKABLE_CRATE) {
                        breakCrate(platform)
                    }
                }
            }
        }

        if (!landed) {
            player.isGrounded = false
        }
    }

    private fun onBellySlamLand(platform: Platform) {
        screenShake(0.35f, 10f)
        audioEngine.playBellySlam()
        vibrate(80)
        spawnShockwave(player.x + player.width / 2, player.y + player.height, Color.White)

        // Shockwave radius stuns nearby enemies
        val shockRadius = 160f
        val px = player.x + player.width / 2
        for (enemy in currentLevel.enemies) {
            if (enemy.isDefeated) continue
            val dist = abs((enemy.x + enemy.width / 2) - px)
            if (dist < shockRadius) {
                stunEnemyWithDizzyBirds(enemy)
                addFloatingText("¡ATURDIDO!", enemy.x, enemy.y - 20f, Color(0xFFFFEB3B))
            }
        }

        // Also damages Boss if nearby
        val boss = currentLevel.boss
        if (boss != null && boss.state != BossState.DEFEATED) {
            val distToBoss = abs((boss.x + boss.width / 2) - px)
            if (distToBoss < shockRadius) {
                damageBoss(1, "¡IMPACTO SÍSMICO!")
            }
        }
    }

    private fun breakCrate(crate: Platform) {
        crate.isBroken = true
        audioEngine.playBellySlam()
        spawnWoodSplinters(crate.x + crate.width / 2, crate.y + crate.height / 2)
        addFloatingText("+50", crate.x, crate.y - 10f, Color(0xFFFFB300))
        player.score += 50
    }

    private fun activateTrapLever(lever: Platform) {
        if (!lever.leverActive) {
            lever.leverActive = true
            lever.triggerTimer = 1.0f
            audioEngine.playPowerup()
            addFloatingText("¡TRAMPA ACTIVADA!", lever.x - 30f, lever.y - 30f, Color(0xFFFF5722))

            // Trigger giant falling haystack above Don Ramiro if in Boss Level
            val boss = currentLevel.boss
            if (boss != null && boss.state != BossState.DEFEATED) {
                projectiles.add(
                    Projectile(
                        id = "giant_hay_${System.nanoTime()}",
                        type = null,
                        source = ProjectileSource.BOSS_HAYBALE,
                        x = boss.x + boss.width / 2 - 40f,
                        y = 60f,
                        vx = 0f,
                        vy = 420f,
                        radius = 42f
                    )
                )
            }
        }
    }

    private fun updatePlatforms(dt: Float) {
        for (platform in currentLevel.platforms) {
            if (platform.type == PlatformType.LEVER_TRAP && platform.leverActive) {
                platform.triggerTimer -= dt
                if (platform.triggerTimer <= 0f) {
                    platform.leverActive = false
                }
            }
        }
    }

    // --- Enemies AI & Stomp Dizzy Birds Mechanic ---

    private fun updateEnemies(dt: Float) {
        var anyChasing = false

        for (enemy in currentLevel.enemies) {
            if (enemy.isDefeated) {
                // Falling off-screen when defeated
                enemy.y += enemy.vy * dt
                enemy.vy += gravity * dt
                continue
            }

            // Stunned State with orbiting dizzy cartoon birds
            if (enemy.isStunned) {
                enemy.stunnedTimer -= dt

                // Update orbiting birds
                for (bird in enemy.dizzyBirds) {
                    bird.angle += bird.speed * dt
                    bird.wingPhase += 18f * dt
                    // Periodic cartoon chirp
                    if (Math.random() < 0.015) {
                        audioEngine.playDizzyBirdChirp()
                    }
                }

                // Recover after timer expires
                if (enemy.stunnedTimer <= 0f) {
                    enemy.isStunned = false
                    addFloatingText("¡Despierta!", enemy.x, enemy.y - 15f, Color.White)
                }
                continue
            }

            // Normal Enemy AI
            enemy.animTime += dt

            // Check if player is near -> Chase Mode!
            val distToPlayer = player.x - enemy.x
            val isPlayerNearby = abs(distToPlayer) < 320f && abs(player.y - enemy.y) < 140f

            if (isPlayerNearby && !player.isInvulnerable()) {
                anyChasing = true
                enemy.facingRight = distToPlayer > 0f
                enemy.vx = (if (enemy.facingRight) 1f else -1f) * (enemy.baseSpeed * 1.35f)
            } else {
                // Patrol behavior
                if (enemy.x <= enemy.patrolMinX) {
                    enemy.facingRight = true
                    enemy.vx = enemy.baseSpeed
                } else if (enemy.x >= enemy.patrolMaxX) {
                    enemy.facingRight = false
                    enemy.vx = -enemy.baseSpeed
                }
            }

            // Rooster hops occasionally
            if (enemy.type == EnemyType.ROOSTER && enemy.isGrounded && Math.random() < 0.02) {
                enemy.vy = -380f
                enemy.isGrounded = false
            }

            // Apply gravity
            enemy.vy += gravity * dt
            enemy.x += enemy.vx * dt
            enemy.y += enemy.vy * dt

            // Ground collision
            checkEnemyGroundCollision(enemy)

            // Collision with Player
            checkPlayerEnemyCollision(enemy)
        }

        // Adapt BGM tension if enemy is chasing
        if (currentLevel.boss == null) {
            if (anyChasing) {
                audioEngine.currentTheme = GameAudioEngine.BgmTheme.CHASE_TENSION
            } else {
                audioEngine.currentTheme = GameAudioEngine.BgmTheme.PEACEFUL_FARM
            }
        }
    }

    private fun checkEnemyGroundCollision(enemy: Enemy) {
        var grounded = false
        for (platform in currentLevel.platforms) {
            if (platform.isBroken) continue
            if (enemy.x + enemy.width > platform.x && enemy.x < platform.x + platform.width) {
                if (enemy.vy >= 0f &&
                    enemy.y + enemy.height >= platform.y &&
                    enemy.y + enemy.height <= platform.y + 24f
                ) {
                    enemy.y = platform.y - enemy.height
                    enemy.vy = 0f
                    grounded = true
                    break
                }
            }
        }
        enemy.isGrounded = grounded
    }

    private fun checkPlayerEnemyCollision(enemy: Enemy) {
        if (enemy.isDefeated) return

        val overlap = boxOverlap(
            player.x, player.y, player.width, player.height,
            enemy.x, enemy.y, enemy.width, enemy.height
        )

        if (!overlap) return

        // Check if player lands on top of enemy (STOMP MECHANIC)
        val playerBottom = player.y + player.height
        val isStomping = player.vy > 0f && (playerBottom <= enemy.y + enemy.height * 0.55f)

        if (isStomping || player.isBellySlamming) {
            // HIGH BOUNCE!
            player.vy = if (player.isBellySlamming) -540f else -600f
            player.isGrounded = false
            player.canDoubleJump = true
            player.didDoubleJump = false
            player.isBellySlamming = false

            audioEngine.playEnemyStomp()
            vibrate(50)
            spawnStompStars(enemy.x + enemy.width / 2, enemy.y)

            // Enemy falls flat with dizzy birds circling overhead!
            stunEnemyWithDizzyBirds(enemy)
            player.score += 100
            addFloatingText("+100", enemy.x, enemy.y - 20f, Color(0xFFFFD54F))

        } else if (!enemy.isStunned && player.invulnerableTimer <= 0f) {
            // Enemy hits player
            damagePlayer(1)
        }
    }

    private fun stunEnemyWithDizzyBirds(enemy: Enemy) {
        enemy.isStunned = true
        enemy.stunnedTimer = 6.5f
        enemy.vx = 0f
        audioEngine.playDizzyBirdChirp()
        spawnDizzyBirds(enemy.x + enemy.width / 2, enemy.y - 10f)
    }

    private fun defeatEnemy(enemy: Enemy) {
        enemy.isDefeated = true
        enemy.isStunned = false
        enemy.vy = -380f
        audioEngine.playOink()
        spawnStompStars(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2)
        player.score += 150
        addFloatingText("¡K.O.!", enemy.x, enemy.y - 20f, Color.White)
    }

    // --- Boss AI ("Don Ramiro, El Granjero") ---

    private fun updateBoss(dt: Float) {
        val boss = currentLevel.boss ?: return
        if (boss.state == BossState.DEFEATED) return

        boss.animTime += dt
        boss.stateTimer += dt
        boss.attackCooldown -= dt

        // Invulnerable flash timer
        if (boss.invulnerableTimer > 0f) {
            boss.invulnerableTimer -= dt
            if (boss.invulnerableTimer <= 0f) {
                boss.isInvulnerable = false
            }
        }

        // Face towards player
        boss.facingRight = player.x > boss.x

        // Boss Phase 2 Enraged
        if (boss.health <= boss.maxHealth / 2 && boss.phase == 1) {
            boss.phase = 2
            screenShake(0.5f, 15f)
            audioEngine.playBossRoar()
            vibrate(100)
            addFloatingText("¡DON RAMIRO SE ENFURECE!", boss.x - 60f, boss.y - 40f, Color.Red)
            spawnShockwave(boss.x + boss.width / 2, boss.y + boss.height / 2, Color.Red)
        }

        // State machine
        when (boss.state) {
            BossState.INTRO -> {
                if (boss.stateTimer > 2.0f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                    boss.attackCooldown = 1.0f
                }
            }
            BossState.IDLE -> {
                // Patrol slowly
                val speed = if (boss.phase == 2) 130f else 85f
                boss.vx = (if (boss.facingRight) 1f else -1f) * speed

                if (boss.attackCooldown <= 0f) {
                    chooseBossAttack(boss)
                }
            }
            BossState.PITCHFORK_PREPARE -> {
                boss.vx = 0f
                if (boss.stateTimer > 0.8f) {
                    boss.state = BossState.PITCHFORK_THROW
                    boss.stateTimer = 0f
                    executePitchforkThrow(boss)
                }
            }
            BossState.PITCHFORK_THROW -> {
                if (boss.stateTimer > 0.6f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                    boss.attackCooldown = if (boss.phase == 2) 1.8f else 2.6f
                }
            }
            BossState.HAY_ROLL_PREPARE -> {
                boss.vx = 0f
                if (boss.stateTimer > 0.7f) {
                    boss.state = BossState.HAY_ROLL
                    boss.stateTimer = 0f
                    executeHayBaleRoll(boss)
                }
            }
            BossState.HAY_ROLL -> {
                if (boss.stateTimer > 0.8f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                    boss.attackCooldown = if (boss.phase == 2) 2.0f else 3.0f
                }
            }
            BossState.GROUND_STOMP_JUMP -> {
                if (boss.stateTimer > 0.5f && boss.y >= 580f) {
                    boss.vy = -620f
                    boss.state = BossState.GROUND_STOMP_SLAM
                    boss.stateTimer = 0f
                    audioEngine.playJump()
                }
            }
            BossState.GROUND_STOMP_SLAM -> {
                // Falling fast
                boss.vy += gravity * 1.5f * dt
                if (boss.y >= 588f) {
                    boss.y = 588f
                    boss.vy = 0f
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                    boss.attackCooldown = if (boss.phase == 2) 1.5f else 2.8f
                    onBossGroundStomp(boss)
                }
            }
            BossState.HURT -> {
                boss.vx = 0f
                if (boss.stateTimer > 0.7f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                }
            }
            BossState.STUNNED -> {
                boss.vx = 0f
                if (boss.stateTimer > 3.0f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                }
            }
            BossState.CHICKEN_CALL -> {
                if (boss.stateTimer > 1.2f) {
                    boss.state = BossState.IDLE
                    boss.stateTimer = 0f
                    boss.attackCooldown = 2.5f
                }
            }
            BossState.DEFEATED -> {
                boss.vx = 0f
            }
        }

        // Apply physics to Boss
        boss.vy += gravity * dt
        boss.x += boss.vx * dt
        boss.y += boss.vy * dt

        // Arena ground & wall collision for Boss
        if (boss.y > 588f) {
            boss.y = 588f
            boss.vy = 0f
        }
        boss.x = boss.x.coerceIn(80f, 1420f)

        // Collision with player
        if (boxOverlap(
                player.x, player.y, player.width, player.height,
                boss.x, boss.y, boss.width, boss.height
            ) && player.invulnerableTimer <= 0f && boss.state != BossState.STUNNED
        ) {
            damagePlayer(1)
        }
    }

    private fun chooseBossAttack(boss: Boss) {
        val choices = if (boss.phase == 2) {
            listOf(BossState.PITCHFORK_PREPARE, BossState.HAY_ROLL_PREPARE, BossState.GROUND_STOMP_JUMP, BossState.CHICKEN_CALL)
        } else {
            listOf(BossState.PITCHFORK_PREPARE, BossState.HAY_ROLL_PREPARE, BossState.GROUND_STOMP_JUMP)
        }
        boss.state = choices.random()
        boss.stateTimer = 0f

        if (boss.state == BossState.CHICKEN_CALL) {
            executeChickenCall(boss)
        }
    }

    private fun executePitchforkThrow(boss: Boss) {
        audioEngine.playThrow()
        val dir = if (boss.facingRight) 1f else -1f
        val forkSpeed = if (boss.phase == 2) 580f else 460f
        projectiles.add(
            Projectile(
                id = "pitchfork_${System.nanoTime()}",
                type = null,
                source = ProjectileSource.BOSS_PITCHFORK,
                x = if (boss.facingRight) boss.x + boss.width else boss.x - 30f,
                y = boss.y + 35f,
                vx = dir * forkSpeed,
                vy = -40f,
                radius = 16f
            )
        )
    }

    private fun executeHayBaleRoll(boss: Boss) {
        audioEngine.playBellySlam()
        val dir = if (boss.facingRight) 1f else -1f
        val rollSpeed = if (boss.phase == 2) 380f else 270f
        projectiles.add(
            Projectile(
                id = "haybale_${System.nanoTime()}",
                type = null,
                source = ProjectileSource.BOSS_HAYBALE,
                x = if (boss.facingRight) boss.x + boss.width else boss.x - 50f,
                y = 630f,
                vx = dir * rollSpeed,
                vy = 0f,
                radius = 28f
            )
        )
    }

    private fun onBossGroundStomp(boss: Boss) {
        screenShake(0.5f, 14f)
        audioEngine.playBossRoar()
        vibrate(80)
        spawnShockwave(boss.x + boss.width / 2, boss.y + boss.height, Color(0xFFFF5722))

        // Ground shockwave trips player if player is on ground
        if (player.isGrounded && player.invulnerableTimer <= 0f) {
            damagePlayer(1)
            player.vy = -340f
            addFloatingText("¡PISOTÓN!", player.x, player.y - 20f, Color.Red)
        }
    }

    private fun executeChickenCall(boss: Boss) {
        audioEngine.playDizzyBirdChirp()
        addFloatingText("¡A POR ÉL!", boss.x, boss.y - 30f, Color.White)
        for (i in 0..2) {
            projectiles.add(
                Projectile(
                    id = "chicken_${System.nanoTime()}_$i",
                    type = null,
                    source = ProjectileSource.BOSS_CHICKEN,
                    x = if (boss.facingRight) boss.x + boss.width + i * 40f else boss.x - 30f - i * 40f,
                    y = 650f,
                    vx = (if (boss.facingRight) 1f else -1f) * (320f + i * 50f),
                    vy = -80f,
                    radius = 14f
                )
            )
        }
    }

    private fun damageBoss(amount: Int, label: String = "¡GOLPE!") {
        val boss = currentLevel.boss ?: return
        if (boss.isInvulnerable || boss.state == BossState.DEFEATED) return

        boss.health -= amount
        boss.isInvulnerable = true
        boss.invulnerableTimer = 1.0f
        boss.state = BossState.HURT
        boss.stateTimer = 0f

        audioEngine.playHurt()
        screenShake(0.3f, 8f)
        vibrate(60)
        addFloatingText(label, boss.x + boss.width / 2, boss.y - 20f, Color(0xFFFFEB3B))

        if (boss.health <= 0) {
            boss.health = 0
            boss.state = BossState.DEFEATED
            audioEngine.playVictory()
            screenShake(0.8f, 18f)
            vibrate(150)
            addFloatingText("¡DON RAMIRO DERROTADO!", boss.x - 50f, boss.y - 40f, Color.Green)
            // Spawn dizzy birds on defeated boss
            spawnDizzyBirds(boss.x + boss.width / 2, boss.y - 15f)
            isLevelCompleted = true
            isGameWon = true
            player.score += 2000
        }
    }

    // --- Projectiles Simulation ---

    private fun updateProjectiles(dt: Float) {
        val it = projectiles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.lifeTimer -= dt
            if (p.lifeTimer <= 0f || !p.active) {
                it.remove()
                continue
            }

            p.rotation += 420f * dt
            if (p.source == ProjectileSource.PLAYER) {
                p.vy += gravity * 0.75f * dt
            } else if (p.source == ProjectileSource.BOSS_PITCHFORK) {
                p.vy += gravity * 0.25f * dt
            } else if (p.source == ProjectileSource.BOSS_HAYBALE) {
                p.vy += gravity * dt
            }

            p.x += p.vx * dt
            p.y += p.vy * dt

            // Check collision with platforms
            var collidedPlatform = false
            for (platform in currentLevel.platforms) {
                if (platform.isBroken) continue
                if (circleBoxCollision(p.x, p.y, p.radius, platform.x, platform.y, platform.width, platform.height)) {
                    collidedPlatform = true
                    if (platform.type == PlatformType.BREAKABLE_CRATE) {
                        breakCrate(platform)
                    }
                    if (platform.type == PlatformType.LEVER_TRAP) {
                        activateTrapLever(platform)
                    }
                    break
                }
            }

            if (collidedPlatform) {
                onProjectileHit(p)
                it.remove()
                continue
            }

            // Player projectiles hitting enemies
            if (p.source == ProjectileSource.PLAYER) {
                var hitEnemy = false
                for (enemy in currentLevel.enemies) {
                    if (enemy.isDefeated) continue
                    if (circleBoxCollision(p.x, p.y, p.radius, enemy.x, enemy.y, enemy.width, enemy.height)) {
                        hitEnemy = true
                        onProjectileHit(p)
                        if (p.type == ThrowableType.MUD) {
                            stunEnemyWithDizzyBirds(enemy)
                            addFloatingText("¡EMBARRIADO!", enemy.x, enemy.y - 20f, Color(0xFF795548))
                        } else {
                            defeatEnemy(enemy)
                        }
                        break
                    }
                }
                if (hitEnemy) {
                    it.remove()
                    continue
                }

                // Player projectiles hitting Boss Don Ramiro
                val boss = currentLevel.boss
                if (boss != null && boss.state != BossState.DEFEATED) {
                    if (circleBoxCollision(p.x, p.y, p.radius, boss.x, boss.y, boss.width, boss.height)) {
                        onProjectileHit(p)
                        val dmg = p.type?.damage ?: 1
                        damageBoss(dmg, "¡${p.type?.displayName?.uppercase() ?: "IMPACTO"}!")
                        it.remove()
                        continue
                    }
                }
            }

            // Boss projectiles hitting player
            if (p.source != ProjectileSource.PLAYER) {
                if (circleBoxCollision(p.x, p.y, p.radius, player.x, player.y, player.width, player.height)) {
                    if (player.invulnerableTimer <= 0f) {
                        damagePlayer(1)
                        onProjectileHit(p)
                        it.remove()
                        continue
                    }
                }

                // Boss falling haystack onto Boss itself (from lever trap)
                val boss = currentLevel.boss
                if (boss != null && p.source == ProjectileSource.BOSS_HAYBALE && p.vy > 300f) {
                    if (circleBoxCollision(p.x, p.y, p.radius, boss.x, boss.y, boss.width, boss.height)) {
                        damageBoss(3, "¡HENO EN LA CABEZA!")
                        boss.state = BossState.STUNNED
                        boss.stateTimer = 0f
                        audioEngine.playBellySlam()
                        it.remove()
                        continue
                    }
                }
            }
        }
    }

    private fun onProjectileHit(p: Projectile) {
        when (p.type) {
            ThrowableType.APPLE -> {
                audioEngine.playSplat()
                spawnAppleSplat(p.x, p.y)
            }
            ThrowableType.CORN -> {
                audioEngine.playPopcorn()
                spawnPopcorn(p.x, p.y)
            }
            ThrowableType.MUD -> {
                audioEngine.playSplat()
                spawnMudSplat(p.x, p.y)
            }
            ThrowableType.ACORN -> {
                audioEngine.playPickup()
                spawnStompStars(p.x, p.y)
            }
            null -> {
                audioEngine.playBellySlam()
                spawnShockwave(p.x, p.y, Color(0xFFFFD54F))
            }
        }
    }

    // --- Collectibles ---

    private fun updateCollectibles(dt: Float) {
        for (col in currentLevel.collectibles) {
            if (col.collected) continue
            col.bobOffset = sin((gameTime * 3.5f) + col.x) * 6f

            if (boxOverlap(
                    player.x, player.y, player.width, player.height,
                    col.x, col.y + col.bobOffset, col.width, col.height
                )
            ) {
                col.collected = true
                audioEngine.playPickup()
                vibrate(20)
                spawnStompStars(col.x + col.width / 2, col.y + col.height / 2)

                when (col.type) {
                    CollectibleType.APPLE -> {
                        player.ammo[ThrowableType.APPLE] = (player.ammo[ThrowableType.APPLE] ?: 0) + 3
                        player.score += 50
                        addFloatingText("+3 🍎", col.x, col.y - 15f, Color(0xFFFF5252))
                    }
                    CollectibleType.CORN -> {
                        player.ammo[ThrowableType.CORN] = (player.ammo[ThrowableType.CORN] ?: 0) + 3
                        player.score += 70
                        addFloatingText("+3 🌽", col.x, col.y - 15f, Color(0xFFFFD700))
                    }
                    CollectibleType.MUD -> {
                        player.ammo[ThrowableType.MUD] = (player.ammo[ThrowableType.MUD] ?: 0) + 3
                        player.score += 50
                        addFloatingText("+3 🟤", col.x, col.y - 15f, Color(0xFF8D6E63))
                    }
                    CollectibleType.ACORN -> {
                        player.ammo[ThrowableType.ACORN] = (player.ammo[ThrowableType.ACORN] ?: 0) + 2
                        player.acorns++
                        player.score += 120
                        addFloatingText("+2 🌰", col.x, col.y - 15f, Color(0xFFFFB300))
                    }
                    CollectibleType.GOLD_COIN -> {
                        player.score += 100
                        addFloatingText("+100", col.x, col.y - 15f, Color(0xFFFFD54F))
                    }
                    CollectibleType.HEART -> {
                        if (player.health < player.maxHealth) {
                            player.health++
                        }
                        player.score += 200
                        addFloatingText("+1 ❤️", col.x, col.y - 15f, Color(0xFFFF1744))
                    }
                }
            }
        }
    }

    // --- Damage & Level Clear ---

    fun damagePlayer(amount: Int) {
        if (player.invulnerableTimer > 0f || isGameOver || isLevelCompleted) return

        player.health -= amount
        player.invulnerableTimer = 1.4f
        player.vy = -340f
        player.vx = if (player.facingRight) -220f else 220f
        audioEngine.playHurt()
        screenShake(0.35f, 10f)
        vibrate(80)

        if (player.health <= 0) {
            player.health = 0
            isGameOver = true
            audioEngine.playGameOver()
            vibrate(140)
            addFloatingText("¡ATRAPADO!", player.x, player.y - 30f, Color.Red)
        }
    }

    private fun checkLevelExit() {
        if (isLevelCompleted) return
        val exitDist = hypot(player.x - currentLevel.exitX, player.y - currentLevel.exitY)
        if (exitDist < 60f && currentLevel.boss == null) {
            isLevelCompleted = true
            audioEngine.playVictory()
            vibrate(80)
            addFloatingText("¡NIVEL COMPLETADO!", player.x - 40f, player.y - 40f, Color.Green)
        }
    }

    private fun updateCamera(dt: Float) {
        // Target camera centered on player
        val targetCamX = (player.x - 300f).coerceIn(0f, (currentLevel.worldWidth - 750f).coerceAtLeast(0f))
        val targetCamY = (player.y - 360f).coerceIn(0f, (currentLevel.worldHeight - 500f).coerceAtLeast(0f))

        cameraX += (targetCamX - cameraX) * 8f * dt
        cameraY += (targetCamY - cameraY) * 6f * dt

        // Screen shake decay
        if (screenShakeTimer > 0f) {
            screenShakeTimer -= dt
        } else {
            screenShakeIntensity = 0f
        }
    }

    fun screenShake(duration: Float, intensity: Float) {
        screenShakeTimer = duration
        screenShakeIntensity = intensity
    }

    private fun vibrate(ms: Long) {
        try {
            vibrator?.vibrate(ms)
        } catch (e: Exception) {
            // Ignore
        }
    }

    // --- Particle Spawners ---

    private fun updateAmbientParticles(dt: Float) {
        // Season-based ambient particles
        if (Math.random() < 0.3) {
            val spawnX = cameraX + (Math.random().toFloat() * 900f)
            val spawnY = cameraY - 20f

            when (currentLevel.season) {
                Season.SPRING -> {
                    // Sakura cherry blossom petals
                    particles.add(
                        GameParticle(
                            x = spawnX,
                            y = spawnY,
                            vx = (Math.random().toFloat() * 80f - 20f) + 40f,
                            vy = (Math.random().toFloat() * 60f) + 40f,
                            color = Color(0xFFFFB6C1),
                            radius = (Math.random().toFloat() * 3f + 3f),
                            life = 6f,
                            maxLife = 6f,
                            type = ParticleType.SAKURA_PETAL,
                            rotSpeed = 120f
                        )
                    )
                }
                Season.SUMMER -> {
                    // Golden dandelion seeds
                    particles.add(
                        GameParticle(
                            x = spawnX,
                            y = spawnY,
                            vx = (Math.random().toFloat() * 60f) + 50f,
                            vy = (Math.random().toFloat() * 30f) + 20f,
                            color = Color(0xFFFFF9C4),
                            radius = (Math.random().toFloat() * 2f + 2f),
                            life = 6f,
                            maxLife = 6f,
                            type = ParticleType.DUST
                        )
                    )
                }
                Season.AUTUMN -> {
                    // Golden and crimson autumn leaves
                    val leafColors = listOf(Color(0xFFFF7043), Color(0xFFFFB300), Color(0xFFD84315))
                    particles.add(
                        GameParticle(
                            x = spawnX,
                            y = spawnY,
                            vx = (Math.random().toFloat() * 110f) + 60f,
                            vy = (Math.random().toFloat() * 70f) + 40f,
                            color = leafColors.random(),
                            radius = (Math.random().toFloat() * 4f + 4f),
                            life = 7f,
                            maxLife = 7f,
                            type = ParticleType.AUTUMN_LEAF,
                            rotSpeed = 180f
                        )
                    )
                }
                Season.WINTER -> {
                    // Drifting snowflakes
                    particles.add(
                        GameParticle(
                            x = spawnX,
                            y = spawnY,
                            vx = (Math.random().toFloat() * 140f) + 80f,
                            vy = (Math.random().toFloat() * 90f) + 50f,
                            color = Color.White,
                            radius = (Math.random().toFloat() * 3f + 2f),
                            life = 5f,
                            maxLife = 5f,
                            type = ParticleType.SNOWFLAKE
                        )
                    )
                }
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.life -= dt
            if (p.life <= 0f) {
                it.remove()
                continue
            }
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            p.rotation += p.rotSpeed * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.type == ParticleType.SHOCKWAVE_RING) {
                p.radius += 280f * dt
            }
        }
    }

    private fun updateFloatingTexts(dt: Float) {
        val it = floatingTexts.iterator()
        while (it.hasNext()) {
            val ft = it.next()
            ft.life -= dt
            if (ft.life <= 0f) {
                it.remove()
                continue
            }
            ft.alpha = (ft.life / 1.0f).coerceIn(0f, 1f)
            ft.y -= 45f * dt
        }
    }

    fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(FloatingText(text, x, y, color))
    }

    private fun spawnRunDust(x: Float, y: Float) {
        particles.add(
            GameParticle(
                x = x,
                y = y - 4f,
                vx = (Math.random().toFloat() * 40f - 20f),
                vy = -(Math.random().toFloat() * 30f + 10f),
                color = Color(0xFFD7CCC8),
                radius = 4f,
                life = 0.35f,
                maxLife = 0.35f,
                type = ParticleType.DUST
            )
        )
    }

    private fun spawnJumpDust(x: Float, y: Float) {
        for (i in 0..6) {
            particles.add(
                GameParticle(
                    x = x + (Math.random().toFloat() * 20f - 10f),
                    y = y,
                    vx = (Math.random().toFloat() * 80f - 40f),
                    vy = -(Math.random().toFloat() * 40f + 10f),
                    color = Color(0xFFBCAAA4),
                    radius = 5f,
                    life = 0.4f,
                    maxLife = 0.4f,
                    type = ParticleType.DUST
                )
            )
        }
    }

    private fun spawnDoubleJumpSparkles(x: Float, y: Float) {
        for (i in 0..10) {
            val angle = (i.toFloat() / 10f) * 2f * PI.toFloat()
            val spd = 90f + Math.random().toFloat() * 60f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFFFFD54F),
                    radius = 4f,
                    life = 0.5f,
                    maxLife = 0.5f,
                    type = ParticleType.STAR_SPARKLE
                )
            )
        }
    }

    private fun spawnShockwave(x: Float, y: Float, color: Color) {
        particles.add(
            GameParticle(
                x = x,
                y = y,
                vx = 0f,
                vy = 0f,
                color = color,
                radius = 10f,
                life = 0.4f,
                maxLife = 0.4f,
                type = ParticleType.SHOCKWAVE_RING
            )
        )
    }

    private fun spawnStompStars(x: Float, y: Float) {
        for (i in 0..7) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val spd = 120f + Math.random().toFloat() * 80f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFFFFEE58),
                    radius = 5f,
                    life = 0.45f,
                    maxLife = 0.45f,
                    type = ParticleType.STAR_SPARKLE
                )
            )
        }
    }

    private fun spawnDizzyBirds(x: Float, y: Float) {
        for (i in 0..5) {
            particles.add(
                GameParticle(
                    x = x + (Math.random().toFloat() * 20f - 10f),
                    y = y,
                    vx = (Math.random().toFloat() * 60f - 30f),
                    vy = -(Math.random().toFloat() * 50f + 20f),
                    color = Color(0xFFFFEB3B),
                    radius = 3.5f,
                    life = 0.6f,
                    maxLife = 0.6f,
                    type = ParticleType.DIZZY_BIRD_TRAIL
                )
            )
        }
    }

    private fun spawnAppleSplat(x: Float, y: Float) {
        for (i in 0..8) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val spd = 90f + Math.random().toFloat() * 70f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFFE53935),
                    radius = 4f,
                    life = 0.5f,
                    maxLife = 0.5f,
                    type = ParticleType.APPLE_CHUNK
                )
            )
        }
    }

    private fun spawnPopcorn(x: Float, y: Float) {
        for (i in 0..10) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val spd = 120f + Math.random().toFloat() * 100f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFFFFFDE7),
                    radius = 5f,
                    life = 0.5f,
                    maxLife = 0.5f,
                    type = ParticleType.POPCORN_BURST
                )
            )
        }
    }

    private fun spawnMudSplat(x: Float, y: Float) {
        for (i in 0..8) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val spd = 70f + Math.random().toFloat() * 60f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFF5D4037),
                    radius = 5f,
                    life = 0.6f,
                    maxLife = 0.6f,
                    type = ParticleType.MUD_SPLAT
                )
            )
        }
    }

    private fun spawnWoodSplinters(x: Float, y: Float) {
        for (i in 0..12) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val spd = 140f + Math.random().toFloat() * 100f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = Color(0xFF8D6E63),
                    radius = 4.5f,
                    life = 0.6f,
                    maxLife = 0.6f,
                    type = ParticleType.DUST
                )
            )
        }
    }

    // --- Math Utilities ---

    private fun boxOverlap(
        x1: Float, y1: Float, w1: Float, h1: Float,
        x2: Float, y2: Float, w2: Float, h2: Float
    ): Boolean {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2
    }

    private fun circleBoxCollision(
        cx: Float, cy: Float, radius: Float,
        bx: Float, by: Float, bw: Float, bh: Float
    ): Boolean {
        val closestX = cx.coerceIn(bx, bx + bw)
        val closestY = cy.coerceIn(by, by + bh)
        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) <= (radius * radius)
    }

    private fun PigPlayer.isInvulnerable(): Boolean = invulnerableTimer > 0f
}
