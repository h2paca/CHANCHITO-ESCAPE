package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.BossState
import com.example.game.model.Season
import com.example.game.model.ThrowableType

@Composable
fun GameHud(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val level = engine.currentLevel
    val boss = level.boss

    // Low health pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (player.health == 1) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top row: Player Stats (Left) - Level Badge (Center) - Settings/Pause (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Hearts & Stamina Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xCC1A1024))
                    .border(1.5.dp, Color(0x55FF6B8B), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Hearts
                Row(
                    modifier = Modifier.scale(if (player.health == 1) heartScale else 1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..player.maxHealth) {
                        Text(
                            text = if (i <= player.health) "❤️" else "🖤",
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Stamina gauge
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x55000000))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(player.stamina / player.maxStamina)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFFC107), Color(0xFFFF9800))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Center: Season Badge & Score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xCC1A1024))
                    .border(1.5.dp, Color(0x55FFD700), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                val seasonIcon = when (level.season) {
                    Season.SPRING -> "🌸"
                    Season.SUMMER -> "☀️"
                    Season.AUTUMN -> "🍁"
                    Season.WINTER -> "❄️"
                }
                Text(
                    text = "$seasonIcon Nivel ${level.id}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "⭐ ${player.score}",
                    color = Color(0xFFFFEB3B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            // Right: Pause & Quick Sound Toggle Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick BGM toggle
                IconButton(
                    onClick = {
                        val current = engine.audioEngine.isBgmOn()
                        engine.audioEngine.setBgmEnabled(!current)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1A1024))
                        .testTag("bgm_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (engine.audioEngine.isBgmOn()) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = "Música",
                        tint = if (engine.audioEngine.isBgmOn()) Color(0xFF69F0AE) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Pause button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1A1024))
                        .testTag("pause_game_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausa",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Second row: Ammo Selector Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ammo selector chip (clickable to switch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xDD2A1B36))
                    .border(1.5.dp, Color(0x77FF6B8B), RoundedCornerShape(16.dp))
                    .clickable { engine.switchThrowable() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("ammo_selector_chip")
            ) {
                Text(
                    text = player.currentThrowable.emoji,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                val count = player.ammo[player.currentThrowable] ?: 0
                Text(
                    text = "${player.currentThrowable.displayName}: $count",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⇄",
                    color = Color(0xFFFF8DA1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Acorns count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC1A1024))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "🌰 ${player.acorns}", color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Boss Health Bar (Only in Boss Level when active)
        if (boss != null && boss.state != BossState.DEFEATED) {
            Spacer(modifier = Modifier.height(6.dp))
            BossHealthBar(boss = boss)
        }
    }
}

@Composable
fun BossHealthBar(boss: com.example.game.model.Boss) {
    val hpFraction = (boss.health.toFloat() / boss.maxHealth).coerceIn(0f, 1f)
    val barColor = if (boss.phase == 2) Color(0xFFFF1744) else Color(0xFFFF9100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD210712))
            .border(2.dp, if (boss.phase == 2) Color.Red else Color(0xFFFFB300), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (boss.phase == 2) "😡 DON RAMIRO (¡FURIOSO!)" else "👨‍🌾 DON RAMIRO, EL GRANJERO",
                    color = if (boss.phase == 2) Color(0xFFFF5252) else Color(0xFFFFE082),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "${boss.health}/${boss.maxHealth} HP",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Health meter bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF3E121E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(hpFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor, if (boss.phase == 2) Color(0xFFFF5252) else Color(0xFFFFD54F))
                        )
                    )
            )
        }
    }
}
