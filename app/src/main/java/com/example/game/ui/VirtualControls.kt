package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine

@Composable
fun VirtualControls(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // --- Left Cluster: Directional Pad (Left, Down/Belly Slam, Right) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Button
            TouchButton(
                icon = "◀",
                label = "",
                bgColors = listOf(Color(0xFF263238), Color(0xFF102027)),
                tag = "btn_left",
                size = 58,
                onDown = { engine.inputLeft = true },
                onUp = { engine.inputLeft = false }
            )

            // Down Button (For ducking or mid-air Salto Bomba)
            TouchButton(
                icon = "▼",
                label = "BOMBA",
                bgColors = listOf(Color(0xFF4527A0), Color(0xFF311B92)),
                tag = "btn_down",
                size = 54,
                onDown = { engine.onDownPressed() },
                onUp = { engine.onDownReleased() }
            )

            // Right Button
            TouchButton(
                icon = "▶",
                label = "",
                bgColors = listOf(Color(0xFF263238), Color(0xFF102027)),
                tag = "btn_right",
                size = 58,
                onDown = { engine.inputRight = true },
                onUp = { engine.inputRight = false }
            )
        }

        // --- Right Cluster: Action Buttons (Run, Shoot/Throw, Jump) ---
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CORRER (Run / Turbo Sprint)
            TouchButton(
                icon = "⚡",
                label = "CORRER",
                bgColors = listOf(Color(0xFFFF8F00), Color(0xFFE65100)),
                tag = "btn_run",
                size = 62,
                onDown = { engine.inputRun = true },
                onUp = { engine.inputRun = false }
            )

            // DISPARAR / LANZAR (Throw current item)
            val ammoCount = engine.player.ammo[engine.player.currentThrowable] ?: 0
            TouchButton(
                icon = engine.player.currentThrowable.emoji,
                label = "LANZAR ($ammoCount)",
                bgColors = listOf(Color(0xFFE91E63), Color(0xFFAD1457)),
                tag = "btn_shoot",
                size = 66,
                onDown = { engine.onThrowPressed() },
                onUp = { /* single-trigger throw */ }
            )

            // SALTAR (Jump / Double Jump / Rebound)
            TouchButton(
                icon = "🦘",
                label = "SALTAR",
                bgColors = listOf(Color(0xFF00C853), Color(0xFF1B5E20)),
                tag = "btn_jump",
                size = 72,
                onDown = { engine.onJumpPressed() },
                onUp = { engine.onJumpReleased() }
            )
        }
    }
}

@Composable
private fun TouchButton(
    icon: String,
    label: String,
    bgColors: List<Color>,
    tag: String,
    size: Int,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .scale(if (isPressed) 0.92f else 1f)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    if (isPressed) bgColors.reversed() else bgColors
                )
            )
            .border(
                width = 2.dp,
                color = if (isPressed) Color.White else Color(0x88FFFFFF),
                shape = CircleShape
            )
            .pointerInput(tag) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onDown()
                    waitForUpOrCancellation()
                    isPressed = false
                    onUp()
                }
            }
            .testTag(tag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = if (size > 60) 22.sp else 18.sp
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (size > 60) 9.sp else 8.sp,
                    maxLines = 1
                )
            }
        }
    }
}
