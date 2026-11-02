package com.example.game.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.game.engine.GameEngine
import com.example.game.model.*
import kotlin.math.*

object GameRenderer {

    fun renderGame(
        drawScope: DrawScope,
        engine: GameEngine,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val level = engine.currentLevel
        val camX = engine.cameraX
        val camY = engine.cameraY

        // Apply screen shake
        val shakeX = if (engine.screenShakeIntensity > 0f) {
            (Math.random().toFloat() * 2f - 1f) * engine.screenShakeIntensity
        } else 0f
        val shakeY = if (engine.screenShakeIntensity > 0f) {
            (Math.random().toFloat() * 2f - 1f) * engine.screenShakeIntensity
        } else 0f

        // Draw parallax sky and scenery
        drawSkyAndParallax(drawScope, level.season, camX, camY, screenWidth, screenHeight)

        // Draw world objects with camera offset
        drawScope.drawContext.canvas.save()
        drawScope.drawContext.canvas.translate(-camX + shakeX, -camY + shakeY)

        // 1. Level Platforms & Traps
        for (platform in level.platforms) {
            drawPlatform(drawScope, platform, level.season)
        }

        // 2. Goal / Exit gate
        if (level.boss == null) {
            drawExitGate(drawScope, level.exitX, level.exitY, engine.gameTime)
        }

        // 3. Collectibles
        for (col in level.collectibles) {
            if (!col.collected) {
                drawCollectible(drawScope, col)
            }
        }

        // 4. Enemies
        for (enemy in level.enemies) {
            drawEnemy(drawScope, enemy)
        }

        // 5. Boss (Don Ramiro)
        if (level.boss != null) {
            drawBoss(drawScope, level.boss)
        }

        // 6. Projectiles
        for (proj in engine.projectiles) {
            drawProjectile(drawScope, proj)
        }

        // 7. Hero Chanchito (Player)
        drawPlayer(drawScope, engine.player)

        // 8. Particles
        for (p in engine.particles) {
            drawParticle(drawScope, p)
        }

        // 9. Floating score/damage texts
        for (ft in engine.floatingTexts) {
            drawScope.drawCircle(
                color = ft.color.copy(alpha = ft.alpha * 0.8f),
                radius = 14f,
                center = Offset(ft.x, ft.y)
            )
        }

        drawScope.drawContext.canvas.restore()
    }

    // --- Parallax Background ---

    private fun drawSkyAndParallax(
        drawScope: DrawScope,
        season: Season,
        camX: Float,
        camY: Float,
        w: Float,
        h: Float
    ) {
        // Gradient sky
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(season.skyColorTop, season.skyColorBottom)
            ),
            size = Size(w, h)
        )

        // Sun / Moon / Winter Blizzard
        when (season) {
            Season.SPRING -> {
                drawScope.drawCircle(
                    color = Color(0xFFFFF59D).copy(alpha = 0.85f),
                    radius = 48f,
                    center = Offset(w * 0.82f, 90f)
                )
            }
            Season.SUMMER -> {
                // Blazing golden summer sun with corona
                drawScope.drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.4f),
                    radius = 70f,
                    center = Offset(w * 0.75f, 85f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFEE58),
                    radius = 46f,
                    center = Offset(w * 0.75f, 85f)
                )
            }
            Season.AUTUMN -> {
                drawScope.drawCircle(
                    color = Color(0xFFFFAB91),
                    radius = 44f,
                    center = Offset(w * 0.78f, 95f)
                )
            }
            Season.WINTER -> {
                // Cold winter moon behind barn
                drawScope.drawCircle(
                    color = Color(0xFFECEFF1).copy(alpha = 0.7f),
                    radius = 42f,
                    center = Offset(w * 0.8f, 100f)
                )
            }
        }

        // Parallax Rolling Hills (Layer 1 - distant, speed 0.15)
        val hillOffset1 = (camX * 0.15f) % 600f
        val hillY1 = h * 0.58f
        val pathHill1 = Path().apply {
            moveTo(0f, h)
            lineTo(0f, hillY1)
            var x = -hillOffset1 - 200f
            while (x < w + 400f) {
                quadraticBezierTo(x + 150f, hillY1 - 70f, x + 300f, hillY1)
                x += 300f
            }
            lineTo(w, h)
            close()
        }
        drawScope.drawPath(pathHill1, color = season.hillColor.copy(alpha = 0.55f))

        // Parallax Rolling Hills (Layer 2 - closer, speed 0.35)
        val hillOffset2 = (camX * 0.35f) % 400f
        val hillY2 = h * 0.68f
        val pathHill2 = Path().apply {
            moveTo(0f, h)
            lineTo(0f, hillY2)
            var x = -hillOffset2 - 200f
            while (x < w + 400f) {
                quadraticBezierTo(x + 100f, hillY2 - 50f, x + 200f, hillY2)
                x += 200f
            }
            lineTo(w, h)
            close()
        }
        drawScope.drawPath(pathHill2, color = season.hillColor.copy(alpha = 0.85f))
    }

    // --- Platforms & Hazards ---

    private fun drawPlatform(drawScope: DrawScope, p: Platform, season: Season) {
        if (p.isBroken) return

        when (p.type) {
            PlatformType.SOLID_GROUND -> {
                // Dirt base
                drawScope.drawRect(
                    color = season.dirtColor,
                    topLeft = Offset(p.x, p.y + 14f),
                    size = Size(p.width, p.height - 14f)
                )
                // Grass / Snow top strip
                drawScope.drawRoundRect(
                    color = season.grassColor,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, 18f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Little grass blades / details
                var gx = p.x + 8f
                while (gx < p.x + p.width - 8f) {
                    drawScope.drawLine(
                        color = season.grassColor.copy(alpha = 0.8f),
                        start = Offset(gx, p.y + 2f),
                        end = Offset(gx + 3f, p.y - 6f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    gx += 18f
                }
            }
            PlatformType.WOOD_PLANK, PlatformType.ONE_WAY -> {
                // Rustic wooden plank
                val woodColor = Color(0xFF8D6E63)
                val woodDark = Color(0xFF5D4037)
                drawScope.drawRoundRect(
                    color = woodColor,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, p.height),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Wood grain lines
                drawScope.drawLine(
                    color = woodDark,
                    start = Offset(p.x + 6f, p.y + p.height / 2),
                    end = Offset(p.x + p.width - 6f, p.y + p.height / 2),
                    strokeWidth = 2f
                )
                // Wooden support posts underneath
                drawScope.drawRect(
                    color = woodDark,
                    topLeft = Offset(p.x + 12f, p.y + p.height),
                    size = Size(10f, 18f)
                )
                drawScope.drawRect(
                    color = woodDark,
                    topLeft = Offset(p.x + p.width - 22f, p.y + p.height),
                    size = Size(10f, 18f)
                )
            }
            PlatformType.BOUNCY_HAY -> {
                // Golden Springy Haystack
                val hayColor = Color(0xFFFFD54F)
                val hayDark = Color(0xFFFFA000)
                drawScope.drawRoundRect(
                    color = hayColor,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, p.height),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Twine rope bindings
                drawScope.drawLine(
                    color = Color(0xFF795548),
                    start = Offset(p.x + 14f, p.y),
                    end = Offset(p.x + 14f, p.y + p.height),
                    strokeWidth = 3f
                )
                drawScope.drawLine(
                    color = Color(0xFF795548),
                    start = Offset(p.x + p.width - 14f, p.y),
                    end = Offset(p.x + p.width - 14f, p.y + p.height),
                    strokeWidth = 3f
                )
                // Spring coils indicator underneath
                drawScope.drawLine(
                    color = Color(0xFFB0BEC5),
                    start = Offset(p.x + p.width / 2, p.y + p.height),
                    end = Offset(p.x + p.width / 2, p.y + p.height + 8f),
                    strokeWidth = 4f
                )
            }
            PlatformType.BREAKABLE_CRATE -> {
                // Wooden crate with cross bracing
                val crateColor = Color(0xFFBCAAA4)
                val crateBorder = Color(0xFF5D4037)
                drawScope.drawRect(
                    color = crateColor,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, p.height)
                )
                drawScope.drawRect(
                    color = crateBorder,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, p.height),
                    style = Stroke(width = 3f)
                )
                // X cross
                drawScope.drawLine(
                    color = crateBorder,
                    start = Offset(p.x, p.y),
                    end = Offset(p.x + p.width, p.y + p.height),
                    strokeWidth = 3f
                )
                drawScope.drawLine(
                    color = crateBorder,
                    start = Offset(p.x + p.width, p.y),
                    end = Offset(p.x, p.y + p.height),
                    strokeWidth = 3f
                )
            }
            PlatformType.SLIPPERY_MUD -> {
                // Wet muddy terrain
                val mudColor = Color(0xFF4E342E)
                val mudHighlight = Color(0xFF6D4C41)
                drawScope.drawRoundRect(
                    color = mudColor,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.width, p.height),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Wet shimmer bubbles
                var sx = p.x + 15f
                while (sx < p.x + p.width - 15f) {
                    drawScope.drawCircle(
                        color = mudHighlight,
                        radius = 4f,
                        center = Offset(sx, p.y + 6f)
                    )
                    sx += 24f
                }
            }
            PlatformType.LEVER_TRAP -> {
                // Trap mechanism lever
                val baseColor = Color(0xFF37474F)
                drawScope.drawRect(
                    color = baseColor,
                    topLeft = Offset(p.x, p.y + 18f),
                    size = Size(p.width, p.height - 18f)
                )
                // Lever stick
                val stickAngle = if (p.leverActive) 25f else -25f
                val pivot = Offset(p.x + p.width / 2, p.y + 20f)
                drawScope.rotate(stickAngle, pivot = pivot) {
                    drawScope.drawLine(
                        color = if (p.leverActive) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        start = pivot,
                        end = Offset(pivot.x, pivot.y - 28f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    // Lever knob
                    drawScope.drawCircle(
                        color = if (p.leverActive) Color(0xFF81C784) else Color(0xFFFF1744),
                        radius = 8f,
                        center = Offset(pivot.x, pivot.y - 28f)
                    )
                }
            }
        }
    }

    private fun drawExitGate(drawScope: DrawScope, x: Float, y: Float, time: Float) {
        // Rustic wooden archway with "LIBERTAD" garland
        val postColor = Color(0xFF5D4037)
        drawScope.drawRect(color = postColor, topLeft = Offset(x - 20f, y - 80f), size = Size(12f, 80f))
        drawScope.drawRect(color = postColor, topLeft = Offset(x + 20f, y - 80f), size = Size(12f, 80f))
        drawScope.drawRect(color = Color(0xFF8D6E63), topLeft = Offset(x - 24f, y - 90f), size = Size(60f, 16f))

        // Glowing victory portal light
        val glowAlpha = (sin(time * 4f) * 0.25f + 0.65f)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF69F0AE).copy(alpha = glowAlpha), Color.Transparent),
                center = Offset(x + 6f, y - 35f),
                radius = 38f
            ),
            center = Offset(x + 6f, y - 35f),
            radius = 38f
        )
    }

    // --- Collectibles ---

    private fun drawCollectible(drawScope: DrawScope, col: Collectible) {
        val cx = col.x + col.width / 2
        val cy = col.y + col.bobOffset + col.height / 2

        when (col.type) {
            CollectibleType.APPLE -> {
                // Juicy Rotten/Wild Apple
                drawScope.drawCircle(color = Color(0xFFE53935), radius = 11f, center = Offset(cx, cy))
                // Leaf and stem
                drawScope.drawLine(
                    color = Color(0xFF5D4037),
                    start = Offset(cx, cy - 9f),
                    end = Offset(cx + 2f, cy - 15f),
                    strokeWidth = 2.5f
                )
                drawScope.drawOval(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(cx + 1f, cy - 16f),
                    size = Size(6f, 4f)
                )
            }
            CollectibleType.CORN -> {
                // Golden Corn Cob
                drawScope.drawRoundRect(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(cx - 7f, cy - 12f),
                    size = Size(14f, 24f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Green husks
                drawScope.drawOval(
                    color = Color(0xFF8BC34A),
                    topLeft = Offset(cx - 10f, cy + 2f),
                    size = Size(8f, 12f)
                )
                drawScope.drawOval(
                    color = Color(0xFF8BC34A),
                    topLeft = Offset(cx + 2f, cy + 2f),
                    size = Size(8f, 12f)
                )
            }
            CollectibleType.MUD -> {
                // Mud Ball
                drawScope.drawCircle(color = Color(0xFF6D4C41), radius = 10f, center = Offset(cx, cy))
                drawScope.drawCircle(color = Color(0xFF8D6E63), radius = 4f, center = Offset(cx - 2f, cy - 2f))
            }
            CollectibleType.ACORN -> {
                // Shiny Golden Acorn
                drawScope.drawCircle(color = Color(0xFFFFD54F), radius = 10f, center = Offset(cx, cy + 2f))
                drawScope.drawArc(
                    color = Color(0xFF5D4037),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - 10f, cy - 8f),
                    size = Size(20f, 12f)
                )
                drawScope.drawLine(
                    color = Color(0xFF3E2723),
                    start = Offset(cx, cy - 8f),
                    end = Offset(cx, cy - 13f),
                    strokeWidth = 2.5f
                )
            }
            CollectibleType.GOLD_COIN -> {
                // Sparkling farm coin
                drawScope.drawCircle(color = Color(0xFFFFD700), radius = 10f, center = Offset(cx, cy))
                drawScope.drawCircle(
                    color = Color(0xFFFFA000),
                    radius = 8f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )
            }
            CollectibleType.HEART -> {
                // Glowing Health Heart
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 6f, center = Offset(cx - 4f, cy - 3f))
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 6f, center = Offset(cx + 4f, cy - 3f))
                val heartPath = Path().apply {
                    moveTo(cx - 10f, cy - 1f)
                    lineTo(cx, cy + 9f)
                    lineTo(cx + 10f, cy - 1f)
                    close()
                }
                drawScope.drawPath(heartPath, color = Color(0xFFFF1744))
            }
        }
    }

    // --- Hero: Chanchito (Piglet) ---

    private fun drawPlayer(drawScope: DrawScope, p: PigPlayer) {
        val cx = p.x + p.width / 2
        val cy = p.y + p.height / 2

        // Blink when invulnerable
        if (p.invulnerableTimer > 0f && (p.animTime * 14f).toInt() % 2 == 0) {
            return
        }

        drawScope.rotate(p.flipAngle, pivot = Offset(cx, cy)) {
            val facing = if (p.facingRight) 1f else -1f

            // Trot / Run bounce
            val runCycle = sin(p.animTime * (if (p.isRunning) 22f else 14f))
            val legOffset = if (p.isGrounded) runCycle * 6f else 0f

            // Belly Slam diving angle pose
            if (p.isBellySlamming) {
                drawBellySlammingPig(drawScope, cx, cy, facing)
                return@rotate
            }

            // 1. Curly Tail behind body
            val tailX = cx - facing * (p.width * 0.44f)
            val tailY = cy - 2f
            drawScope.drawArc(
                color = Color(0xFFFF8DA1),
                startAngle = if (p.facingRight) 120f else -60f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(tailX - 6f, tailY - 6f),
                size = Size(12f, 12f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 2. Trotters / Hooves (Legs)
            val legColor = Color(0xFFD81B60)
            // Back legs
            drawScope.drawRoundRect(
                color = legColor,
                topLeft = Offset(cx - facing * 8f - 4f, cy + 8f + legOffset),
                size = Size(7f, 10f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Front legs
            drawScope.drawRoundRect(
                color = legColor,
                topLeft = Offset(cx + facing * 8f - 4f, cy + 8f - legOffset),
                size = Size(7f, 10f),
                cornerRadius = CornerRadius(3f, 3f)
            )

            // 3. Chubby Pig Body
            val bodyColor = Color(0xFFFF80AB)
            drawScope.drawOval(
                color = bodyColor,
                topLeft = Offset(cx - p.width * 0.5f, cy - p.height * 0.42f),
                size = Size(p.width, p.height * 0.84f)
            )

            // 4. Belly highlight
            drawScope.drawOval(
                color = Color(0xFFFFB2C9),
                topLeft = Offset(cx - p.width * 0.35f, cy - p.height * 0.1f),
                size = Size(p.width * 0.7f, p.height * 0.5f)
            )

            // 5. Hero Red Bandana around neck
            val bandanaX = cx + facing * 4f
            val bandanaY = cy - 4f
            drawScope.drawRoundRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(bandanaX - 10f, bandanaY - 4f),
                size = Size(20f, 9f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Bandana knot tails fluttering
            val tailFlutter = sin(p.animTime * 16f) * 4f
            val knotPath = Path().apply {
                moveTo(bandanaX - facing * 8f, bandanaY)
                lineTo(bandanaX - facing * 18f, bandanaY - 6f + tailFlutter)
                lineTo(bandanaX - facing * 16f, bandanaY + 4f + tailFlutter)
                close()
            }
            drawScope.drawPath(knotPath, color = Color(0xFFB71C1C))

            // 6. Cute Pig Ears
            val earColor = Color(0xFFFF4081)
            val earInner = Color(0xFFFF80AB)
            // Ear 1
            val ear1X = cx + facing * 6f
            val ear1Y = cy - p.height * 0.44f
            val earPath1 = Path().apply {
                moveTo(ear1X - 5f, ear1Y)
                lineTo(ear1X + 2f, ear1Y - 14f)
                lineTo(ear1X + 8f, ear1Y)
                close()
            }
            drawScope.drawPath(earPath1, color = earColor)
            // Ear 2
            val ear2X = cx - facing * 6f
            val ear2Y = cy - p.height * 0.44f
            val earPath2 = Path().apply {
                moveTo(ear2X - 6f, ear2Y)
                lineTo(ear2X, ear2Y - 12f)
                lineTo(ear2X + 6f, ear2Y)
                close()
            }
            drawScope.drawPath(earPath2, color = earInner)

            // 7. Expressive Pig Eyes
            val eyeX = cx + facing * 7f
            val eyeY = cy - 8f
            drawScope.drawCircle(color = Color(0xFF263238), radius = 3.5f, center = Offset(eyeX, eyeY))
            // Eye shine
            drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(eyeX + facing * 1f, eyeY - 1f))

            // 8. Rosy Cheek
            drawScope.drawCircle(
                color = Color(0xFFFF5252).copy(alpha = 0.55f),
                radius = 4f,
                center = Offset(cx + facing * 4f, cy)
            )

            // 9. Pig Snout with 2 nostrils
            val snoutX = cx + facing * (p.width * 0.35f)
            val snoutY = cy - 1f
            drawScope.drawOval(
                color = Color(0xFFFF4081),
                topLeft = Offset(snoutX - 7f, snoutY - 5f),
                size = Size(14f, 10f)
            )
            // Nostrils
            drawScope.drawCircle(
                color = Color(0xFF880E4F),
                radius = 1.5f,
                center = Offset(snoutX - facing * 2f, snoutY)
            )
            drawScope.drawCircle(
                color = Color(0xFF880E4F),
                radius = 1.5f,
                center = Offset(snoutX + facing * 2f, snoutY)
            )
        }
    }

    private fun drawBellySlammingPig(drawScope: DrawScope, cx: Float, cy: Float, facing: Float) {
        // Dramatic belly diving pose downwards!
        val bodyColor = Color(0xFFFF80AB)
        val legColor = Color(0xFFD81B60)

        // Limbs spread out
        drawScope.drawCircle(color = legColor, radius = 5f, center = Offset(cx - 14f, cy - 12f))
        drawScope.drawCircle(color = legColor, radius = 5f, center = Offset(cx + 14f, cy - 12f))
        drawScope.drawCircle(color = legColor, radius = 5f, center = Offset(cx - 12f, cy + 12f))
        drawScope.drawCircle(color = legColor, radius = 5f, center = Offset(cx + 12f, cy + 12f))

        // Big round belly
        drawScope.drawCircle(color = bodyColor, radius = 18f, center = Offset(cx, cy))
        drawScope.drawCircle(color = Color(0xFFFFB2C9), radius = 12f, center = Offset(cx, cy + 2f))

        // Snout pointing down
        drawScope.drawOval(
            color = Color(0xFFFF4081),
            topLeft = Offset(cx - 6f, cy + 10f),
            size = Size(12f, 8f)
        )
        // Red bandana cape fluttering upwards
        val capePath = Path().apply {
            moveTo(cx - 10f, cy - 14f)
            lineTo(cx, cy - 26f)
            lineTo(cx + 10f, cy - 14f)
            close()
        }
        drawScope.drawPath(capePath, color = Color(0xFFD32F2F))
    }

    // --- Enemies: Chibis with Knives & Roosters ---

    private fun drawEnemy(drawScope: DrawScope, enemy: Enemy) {
        val cx = enemy.x + enemy.width / 2
        val cy = enemy.y + enemy.height / 2

        if (enemy.isDefeated) {
            // Defeated spinning out
            drawScope.rotate(enemy.y * 2f, pivot = Offset(cx, cy)) {
                drawEnemySprite(drawScope, enemy, cx, cy, isStunned = true)
            }
            return
        }

        if (enemy.isStunned) {
            // Knocked flat on back with swirling eyes and orbiting cartoon birds!
            drawEnemySprite(drawScope, enemy, cx, cy + 8f, isStunned = true)

            // Orbiting Dizzy Cartoon Birds! (As requested)
            drawOrbitingDizzyBirds(drawScope, enemy, cx, cy - 12f)
        } else {
            drawEnemySprite(drawScope, enemy, cx, cy, isStunned = false)
        }
    }

    private fun drawEnemySprite(
        drawScope: DrawScope,
        enemy: Enemy,
        cx: Float,
        cy: Float,
        isStunned: Boolean
    ) {
        val facing = if (enemy.facingRight) 1f else -1f

        when (enemy.type) {
            EnemyType.CHIBI_KNIFE, EnemyType.CHIBI_FORK -> {
                // Cute Chibi Farm Kid
                val skinColor = Color(0xFFFFE0B2)
                val shirtColor = if (enemy.type == EnemyType.CHIBI_KNIFE) Color(0xFF42A5F5) else Color(0xFF66BB6A)
                val overallColor = Color(0xFF1565C0)
                val hatColor = Color(0xFFFFCA28)

                if (isStunned) {
                    // Knocked down on back
                    drawScope.drawRoundRect(
                        color = overallColor,
                        topLeft = Offset(cx - 18f, cy - 4f),
                        size = Size(36f, 14f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Head on side
                    drawScope.drawCircle(color = skinColor, radius = 10f, center = Offset(cx - 14f, cy - 6f))
                    // Straw hat knocked askew
                    drawScope.drawOval(
                        color = hatColor,
                        topLeft = Offset(cx - 24f, cy - 18f),
                        size = Size(20f, 10f)
                    )
                    // Swirl Eyes (@_@)
                    drawScope.drawCircle(
                        color = Color(0xFF263238),
                        radius = 3f,
                        center = Offset(cx - 14f, cy - 6f),
                        style = Stroke(width = 1.5f)
                    )
                    // Dropped cutlery on floor
                    drawScope.drawRect(
                        color = Color(0xFFCFD8DC),
                        topLeft = Offset(cx + 8f, cy + 2f),
                        size = Size(14f, 4f)
                    )
                } else {
                    val legWalk = sin(enemy.animTime * 14f) * 6f

                    // Legs
                    drawScope.drawRect(color = Color(0xFF0D47A1), topLeft = Offset(cx - 6f, cy + 8f + legWalk), size = Size(5f, 12f))
                    drawScope.drawRect(color = Color(0xFF0D47A1), topLeft = Offset(cx + 1f, cy + 8f - legWalk), size = Size(5f, 12f))

                    // Chibi Body & Overalls
                    drawScope.drawRoundRect(
                        color = shirtColor,
                        topLeft = Offset(cx - 10f, cy - 6f),
                        size = Size(20f, 16f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawScope.drawRect(
                        color = overallColor,
                        topLeft = Offset(cx - 8f, cy + 2f),
                        size = Size(16f, 8f)
                    )

                    // Big Chibi Head
                    drawScope.drawCircle(color = skinColor, radius = 12f, center = Offset(cx, cy - 14f))

                    // Oversized Straw Hat
                    drawScope.drawOval(
                        color = hatColor,
                        topLeft = Offset(cx - 18f, cy - 28f),
                        size = Size(36f, 12f)
                    )
                    drawScope.drawArc(
                        color = Color(0xFFFFB300),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx - 10f, cy - 35f),
                        size = Size(20f, 16f)
                    )

                    // Determined Chibi Eyes
                    val eyeX = cx + facing * 4f
                    val eyeY = cy - 14f
                    drawScope.drawCircle(color = Color(0xFF263238), radius = 2.5f, center = Offset(eyeX, eyeY))

                    // Weapon in Hand: Knife or Fork!
                    val handX = cx + facing * 12f
                    val handY = cy
                    if (enemy.type == EnemyType.CHIBI_KNIFE) {
                        // Shiny kitchen knife
                        drawScope.drawRect(
                            color = Color(0xFFECEFF1),
                            topLeft = Offset(handX, handY - 10f),
                            size = Size(facing * 14f, 5f)
                        )
                        drawScope.drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(handX - facing * 4f, handY - 9f),
                            size = Size(facing * 5f, 3f)
                        )
                    } else {
                        // Wooden Fork
                        drawScope.drawRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(handX, handY - 14f),
                            size = Size(3f, 16f)
                        )
                    }
                }
            }
            EnemyType.ROOSTER -> {
                // Rooster
                val roosterColor = Color(0xFF8D6E63)
                drawScope.drawOval(color = roosterColor, topLeft = Offset(cx - 12f, cy - 8f), size = Size(24f, 18f))
                // Red crest
                drawScope.drawCircle(color = Color(0xFFD32F2F), radius = 5f, center = Offset(cx + facing * 8f, cy - 14f))
                // Beak
                drawScope.drawArc(
                    color = Color(0xFFFFB300),
                    startAngle = if (enemy.facingRight) -30f else 150f,
                    sweepAngle = 60f,
                    useCenter = true,
                    topLeft = Offset(cx + facing * 10f - 4f, cy - 10f),
                    size = Size(8f, 6f)
                )
            }
            EnemyType.CHICKEN -> {
                // Baby Hen
                drawScope.drawCircle(color = Color(0xFFFFF59D), radius = 10f, center = Offset(cx, cy))
                drawScope.drawCircle(color = Color(0xFFFFB300), radius = 2f, center = Offset(cx + facing * 8f, cy))
            }
        }
    }

    private fun drawOrbitingDizzyBirds(drawScope: DrawScope, enemy: Enemy, cx: Float, cy: Float) {
        // Render 3 little cartoon yellow birds orbiting in 3D perspective with wing flaps
        for (bird in enemy.dizzyBirds) {
            val orbitRadiusX = 24f
            val orbitRadiusY = 9f
            val bx = cx + cos(bird.angle) * orbitRadiusX
            val by = cy + sin(bird.angle) * orbitRadiusY

            // Draw yellow cartoon bird (body, head, beak, flapping wings!)
            val birdColor = Color(0xFFFFEB3B)
            val beakColor = Color(0xFFFF9800)

            // Little yellow body
            drawScope.drawOval(
                color = birdColor,
                topLeft = Offset(bx - 5f, by - 3f),
                size = Size(10f, 7f)
            )
            // Head
            drawScope.drawCircle(
                color = birdColor,
                radius = 4f,
                center = Offset(bx + 3f, by - 2f)
            )
            // Orange beak
            drawScope.drawArc(
                color = beakColor,
                startAngle = -20f,
                sweepAngle = 40f,
                useCenter = true,
                topLeft = Offset(bx + 5f, by - 3f),
                size = Size(5f, 4f)
            )
            // Eye
            drawScope.drawCircle(
                color = Color.Black,
                radius = 1f,
                center = Offset(bx + 3f, by - 3f)
            )

            // Flapping little wings
            val wingFlap = sin(bird.wingPhase) * 4f
            val wingPath = Path().apply {
                moveTo(bx - 2f, by)
                lineTo(bx, by - 6f + wingFlap)
                lineTo(bx + 3f, by)
                close()
            }
            drawScope.drawPath(wingPath, color = Color(0xFFFDD835))

            // Sparkle star trail
            drawScope.drawCircle(
                color = Color(0xFFFFEE58).copy(alpha = 0.6f),
                radius = 2f,
                center = Offset(bx - 6f, by + 2f)
            )
        }
    }

    // --- Boss: Don Ramiro, El Granjero ---

    private fun drawBoss(drawScope: DrawScope, boss: Boss) {
        val cx = boss.x + boss.width / 2
        val cy = boss.y + boss.height / 2
        val facing = if (boss.facingRight) 1f else -1f

        if (boss.isInvulnerable && (boss.animTime * 14f).toInt() % 2 == 0) {
            return
        }

        // Heavy Boots
        drawScope.drawRoundRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(cx - 24f, cy + 30f),
            size = Size(20f, 16f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawScope.drawRoundRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(cx + 4f, cy + 30f),
            size = Size(20f, 16f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Denim Overalls (Phase 2 enraged turns darker/red tinted)
        val overallColor = if (boss.phase == 2) Color(0xFFB71C1C) else Color(0xFF1976D2)
        val shirtColor = if (boss.phase == 2) Color(0xFFFF5722) else Color(0xFFECEFF1)

        // Arms and Plaid Shirt
        drawScope.drawRoundRect(
            color = shirtColor,
            topLeft = Offset(cx - boss.width * 0.42f, cy - 20f),
            size = Size(boss.width * 0.84f, 40f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Denim Bib Overalls
        drawScope.drawRoundRect(
            color = overallColor,
            topLeft = Offset(cx - boss.width * 0.35f, cy - 5f),
            size = Size(boss.width * 0.7f, 38f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Brass overall buckles
        drawScope.drawCircle(color = Color(0xFFFFD700), radius = 3.5f, center = Offset(cx - 14f, cy - 3f))
        drawScope.drawCircle(color = Color(0xFFFFD700), radius = 3.5f, center = Offset(cx + 14f, cy - 3f))

        // Huge Farmer Head
        val skinColor = if (boss.phase == 2) Color(0xFFFF8A80) else Color(0xFFFFCCBC)
        drawScope.drawCircle(color = skinColor, radius = 22f, center = Offset(cx, cy - 26f))

        // Big Bushy Mustache
        val mustachePath = Path().apply {
            moveTo(cx - 18f, cy - 18f)
            quadraticBezierTo(cx, cy - 12f, cx + 18f, cy - 18f)
            quadraticBezierTo(cx, cy - 6f, cx - 18f, cy - 18f)
            close()
        }
        drawScope.drawPath(mustachePath, color = Color(0xFF4E342E))

        // Angry Eyebrows & Eyes
        val browY = cy - 32f
        drawScope.drawLine(
            color = Color(0xFF3E2723),
            start = Offset(cx - 14f, browY - 3f),
            end = Offset(cx - 3f, browY + 2f),
            strokeWidth = 3.5f
        )
        drawScope.drawLine(
            color = Color(0xFF3E2723),
            start = Offset(cx + 14f, browY - 3f),
            end = Offset(cx + 3f, browY + 2f),
            strokeWidth = 3.5f
        )
        drawScope.drawCircle(color = Color(0xFF212121), radius = 3f, center = Offset(cx - 8f, browY + 4f))
        drawScope.drawCircle(color = Color(0xFF212121), radius = 3f, center = Offset(cx + 8f, browY + 4f))

        // Big Straw Hat with Red Ribbon
        drawScope.drawOval(
            color = Color(0xFFFFD54F),
            topLeft = Offset(cx - 36f, cy - 48f),
            size = Size(72f, 18f)
        )
        drawScope.drawArc(
            color = Color(0xFFFFC107),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 22f, cy - 60f),
            size = Size(44f, 30f)
        )
        drawScope.drawRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(cx - 22f, cy - 47f),
            size = Size(44f, 6f)
        )

        // Pitchfork in hand
        val forkX = cx + facing * 32f
        val forkY = cy - 10f
        // Wooden handle
        drawScope.drawLine(
            color = Color(0xFF6D4C41),
            start = Offset(forkX, forkY - 45f),
            end = Offset(forkX, forkY + 45f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        // Iron tines
        val tineColor = Color(0xFFCFD8DC)
        drawScope.drawLine(
            color = tineColor,
            start = Offset(forkX - 10f, forkY - 45f),
            end = Offset(forkX + 10f, forkY - 45f),
            strokeWidth = 4f
        )
        drawScope.drawLine(color = tineColor, start = Offset(forkX - 10f, forkY - 45f), end = Offset(forkX - 10f, forkY - 65f), strokeWidth = 3f)
        drawScope.drawLine(color = tineColor, start = Offset(forkX, forkY - 45f), end = Offset(forkX, forkY - 67f), strokeWidth = 3.5f)
        drawScope.drawLine(color = tineColor, start = Offset(forkX + 10f, forkY - 45f), end = Offset(forkX + 10f, forkY - 65f), strokeWidth = 3f)

        // If defeated, draw dizzy stars on Don Ramiro
        if (boss.state == BossState.DEFEATED) {
            for (i in 0..4) {
                val angle = boss.animTime * 4f + i * 1.25f
                val sx = cx + cos(angle) * 32f
                val sy = cy - 40f + sin(angle) * 12f
                drawScope.drawCircle(color = Color(0xFFFFD700), radius = 5f, center = Offset(sx, sy))
            }
        }
    }

    // --- Projectiles ---

    private fun drawProjectile(drawScope: DrawScope, p: Projectile) {
        val cx = p.x
        val cy = p.y

        drawScope.rotate(p.rotation, pivot = Offset(cx, cy)) {
            when (p.source) {
                ProjectileSource.PLAYER -> {
                    when (p.type) {
                        ThrowableType.APPLE -> {
                            drawScope.drawCircle(color = Color(0xFFE53935), radius = 10f, center = Offset(cx, cy))
                            drawScope.drawCircle(color = Color(0xFFB71C1C), radius = 4f, center = Offset(cx - 3f, cy - 2f))
                        }
                        ThrowableType.CORN -> {
                            drawScope.drawRoundRect(
                                color = Color(0xFFFFC107),
                                topLeft = Offset(cx - 6f, cy - 12f),
                                size = Size(12f, 24f),
                                cornerRadius = CornerRadius(5f, 5f)
                            )
                        }
                        ThrowableType.MUD -> {
                            drawScope.drawCircle(color = Color(0xFF5D4037), radius = 9f, center = Offset(cx, cy))
                        }
                        ThrowableType.ACORN -> {
                            drawScope.drawCircle(color = Color(0xFFFFD54F), radius = 9f, center = Offset(cx, cy))
                        }
                        null -> {}
                    }
                }
                ProjectileSource.BOSS_PITCHFORK -> {
                    // Flying spinning iron pitchfork
                    drawScope.drawLine(
                        color = Color(0xFF6D4C41),
                        start = Offset(cx - 24f, cy),
                        end = Offset(cx + 24f, cy),
                        strokeWidth = 4f
                    )
                    drawScope.drawLine(
                        color = Color(0xFFCFD8DC),
                        start = Offset(cx + 18f, cy - 8f),
                        end = Offset(cx + 28f, cy),
                        strokeWidth = 3f
                    )
                    drawScope.drawLine(
                        color = Color(0xFFCFD8DC),
                        start = Offset(cx + 18f, cy + 8f),
                        end = Offset(cx + 28f, cy),
                        strokeWidth = 3f
                    )
                }
                ProjectileSource.BOSS_HAYBALE -> {
                    // Huge rolling round haybale
                    drawScope.drawCircle(color = Color(0xFFFFD54F), radius = p.radius, center = Offset(cx, cy))
                    drawScope.drawCircle(
                        color = Color(0xFFFFA000),
                        radius = p.radius * 0.7f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFFA000),
                        radius = p.radius * 0.35f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )
                }
                ProjectileSource.BOSS_CHICKEN -> {
                    drawScope.drawCircle(color = Color(0xFFFFF59D), radius = p.radius, center = Offset(cx, cy))
                    drawScope.drawCircle(color = Color(0xFFFF5722), radius = 4f, center = Offset(cx + 6f, cy - 4f))
                }
            }
        }
    }

    // --- Particles ---

    private fun drawParticle(drawScope: DrawScope, p: GameParticle) {
        val alphaColor = p.color.copy(alpha = p.alpha)

        when (p.type) {
            ParticleType.SHOCKWAVE_RING -> {
                drawScope.drawCircle(
                    color = alphaColor,
                    radius = p.radius,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 4f)
                )
            }
            ParticleType.STAR_SPARKLE -> {
                // 4-point star
                val r = p.radius
                val starPath = Path().apply {
                    moveTo(p.x, p.y - r * 1.5f)
                    lineTo(p.x + r * 0.4f, p.y - r * 0.4f)
                    lineTo(p.x + r * 1.5f, p.y)
                    lineTo(p.x + r * 0.4f, p.y + r * 0.4f)
                    lineTo(p.x, p.y + r * 1.5f)
                    lineTo(p.x - r * 0.4f, p.y + r * 0.4f)
                    lineTo(p.x - r * 1.5f, p.y)
                    lineTo(p.x - r * 0.4f, p.y - r * 0.4f)
                    close()
                }
                drawScope.drawPath(starPath, color = alphaColor)
            }
            ParticleType.SAKURA_PETAL, ParticleType.AUTUMN_LEAF -> {
                drawScope.rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                    drawScope.drawOval(
                        color = alphaColor,
                        topLeft = Offset(p.x - p.radius, p.y - p.radius * 0.6f),
                        size = Size(p.radius * 2f, p.radius * 1.2f)
                    )
                }
            }
            else -> {
                drawScope.drawCircle(
                    color = alphaColor,
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}
