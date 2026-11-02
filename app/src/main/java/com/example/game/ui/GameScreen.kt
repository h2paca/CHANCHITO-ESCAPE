package com.example.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    engine: GameEngine,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPaused by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(1000f) }
    var screenHeight by remember { mutableStateOf(800f) }
    var frameTick by remember { mutableStateOf(0L) }

    // Intercept back button to show pause menu
    BackHandler {
        if (!isPaused) {
            isPaused = true
        } else {
            onBackToMenu()
        }
    }

    // 60 FPS High-Precision Game Loop
    LaunchedEffect(isPaused) {
        if (isPaused) return@LaunchedEffect
        var lastNanos = System.nanoTime()

        while (isActive) {
            withFrameNanos { nowNanos ->
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nowNanos

                engine.update(dt)
                frameTick = nowNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Game World Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            screenWidth = size.width
            screenHeight = size.height

            // Trigger recomposition on frameTick
            if (frameTick >= 0L) {
                GameRenderer.renderGame(
                    drawScope = this,
                    engine = engine,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            }
        }

        // Active Tutorial Hints Banner on bottom-center (if any)
        val currentHint = engine.currentLevel.hints.firstOrNull { (hx, _) ->
            kotlin.math.abs(engine.player.x - hx) < 160f
        }
        if (currentHint != null && !isPaused && !engine.isGameOver && !engine.isLevelCompleted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .background(Color(0xDD1C1226), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💡 ${currentHint.second}",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Modern HUD
        GameHud(
            engine = engine,
            onPauseClick = { isPaused = true },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom On-Screen Virtual Touch Controls
        VirtualControls(
            engine = engine,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Overlays & Dialogs

        // 1. Pause Menu
        if (isPaused) {
            PauseMenuDialog(
                engine = engine,
                onResume = { isPaused = false },
                onRestart = {
                    isPaused = false
                    engine.restartCurrentLevel()
                },
                onExitMenu = {
                    isPaused = false
                    onBackToMenu()
                }
            )
        }

        // 2. Game Over
        if (engine.isGameOver) {
            GameOverDialog(
                onRetry = { engine.restartCurrentLevel() },
                onExitMenu = onBackToMenu
            )
        }

        // 3. Level Clear Celebration
        if (engine.isLevelCompleted && !engine.isGameWon) {
            LevelClearDialog(
                engine = engine,
                onNextLevel = { engine.nextLevel() },
                onReplay = { engine.restartCurrentLevel() }
            )
        }

        // 4. Freedom Victory Screen (Boss Defeated / All levels won!)
        if (engine.isGameWon) {
            VictoryFreedomDialog(
                score = engine.player.score,
                onPlayAgain = {
                    engine.loadLevel(1)
                }
            )
        }
    }
}
