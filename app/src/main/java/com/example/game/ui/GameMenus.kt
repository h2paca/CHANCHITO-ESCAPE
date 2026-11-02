package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.engine.GameEngine
import com.example.game.levels.LevelRepository
import com.example.game.model.Season

@Composable
fun MainMenuScreen(
    engine: GameEngine,
    onStartGame: () -> Unit,
    onSelectLevel: (Int) -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showLevelSelect by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF140C1D))
    ) {
        // Banner Hero Image
        Image(
            painter = painterResource(id = R.drawable.farm_hero_banner),
            contentDescription = "Chanchito Escape Banner",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for smooth contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x77140C1D),
                            Color(0xF0140C1D),
                            Color(0xFF140C1D)
                        ),
                        startY = 180f
                    )
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title & Subtitle Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xD92D112B))
                    .border(2.dp, Color(0xFFFF6B8B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🐷 CHANCHITO ESCAPE 🏃‍♂️",
                        color = Color(0xFFFF80AB),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "¡Huye de la granja antes del festín de Don Ramiro!",
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Play Button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4081)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡JUGAR AHORA!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Buttons: Level Select & How to Play
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showLevelSelect = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("select_level_btn"),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF7043)))),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Niveles", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = { showHowToPlay = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("how_to_play_btn"),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFF69F0AE), Color(0xFF00B0FF)))),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Tutorial", color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Modals
        if (showHowToPlay) {
            HowToPlayDialog(onDismiss = { showHowToPlay = false })
        }

        if (showLevelSelect) {
            LevelSelectDialog(
                currentLevelId = engine.currentLevel.id,
                onSelect = { id ->
                    showLevelSelect = false
                    onSelectLevel(id)
                },
                onDismiss = { showLevelSelect = false }
            )
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF24142F))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📖 CÓMO JUGAR",
                    color = Color(0xFFFF80AB),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                TutorialRow("◀ ▶", "Movimiento", "Corre por los niveles rurales estacionales.")
                TutorialRow("⚡ CORRER", "Turbo Sprint", "Aumenta la velocidad y longitud de salto (usa estamina).")
                TutorialRow("🦘 SALTAR", "Doble Salto", "Pulsa en el suelo para saltar, y de nuevo en el aire para doble salto acrobático!")
                TutorialRow("▼ BOMBA", "Salto Bomba", "Pulsa ABAJO en el aire para caer con fuerza, rompiendo cajas y aturdiendo enemigos cercanos!")
                TutorialRow("⭐ PISOTÓN", "Pisar Enemigos", "¡Salta sobre los niños chibi con cuchillos! Caerán al suelo con pajaritos mareados volando!")
                TutorialRow("🎯 LANZAR", "Proyectiles", "Lanza Manzanas 🍎, Maíz 🌽, Lodo 🟤 y Bellotas 🌰 recolectadas.")
                TutorialRow("👨‍🌾 JEFE", "Don Ramiro", "Esquiva sus horcas y fardos, activa las palancas para arrojarle heno!")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡ENTENDIDO!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TutorialRow(badge: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(76.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3B1D4B))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(badge, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, color = Color(0xFFCFD8DC), fontSize = 11.sp)
        }
    }
}

@Composable
fun LevelSelectDialog(
    currentLevelId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1029))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🗺️ SELECCIONA TEMPORADA",
                    color = Color(0xFFFFD54F),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(14.dp))

                for (id in 1..4) {
                    val level = LevelRepository.getLevel(id)
                    val isCurrent = id == currentLevelId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color(0xFF4A148C) else Color(0xFF2C1B3B))
                            .border(1.5.dp, if (isCurrent) Color(0xFFFF4081) else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { onSelect(id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (level.season) {
                            Season.SPRING -> "🌸"
                            Season.SUMMER -> "☀️"
                            Season.AUTUMN -> "🍁"
                            Season.WINTER -> "❄️"
                        }
                        Text(icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = level.subtitle,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        if (id == 4) {
                            Text("👑 JEFE", color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    engine: GameEngine,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExitMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF21112D))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏸️ JUEGO EN PAUSA",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Audio Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF331C44))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Música BGM", color = Color.White, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = engine.audioEngine.isBgmOn(),
                        onCheckedChange = { engine.audioEngine.setBgmEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF331C44))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Efectos SFX", color = Color.White, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = engine.audioEngine.isSfxOn(),
                        onCheckedChange = { engine.audioEngine.setSfxEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reiniciar Nivel", color = Color(0xFFFFD54F))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onExitMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Menú Principal", color = Color(0xFFFF8A80))
                }
            }
        }
    }
}

@Composable
fun LevelClearDialog(
    engine: GameEngine,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A1E))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 ¡NIVEL COMPLETADO!",
                    color = Color(0xFF69F0AE),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Chanchito escapó del peligro estacional!",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Stars rating
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⭐", fontSize = 32.sp)
                    Text("⭐", fontSize = 32.sp)
                    Text("⭐", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E4833))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Puntos: ${engine.player.score}",
                        color = Color(0xFFFFD54F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("SIGUIENTE NIVEL ➔", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onReplay,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Repetir", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun VictoryFreedomDialog(
    score: Int,
    onPlayAgain: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14201A))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.farm_victory_banner),
                    contentDescription = "¡Victoria Chanchito Libre!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🏆 ¡CHANCHITO ES LIBRE!",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "¡Derrotaste al granjero Don Ramiro y sus trampas! Ahora nuestro cerdito héroe vive feliz y en paz en el bosque silvestre con sus amigos.",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Puntuación Final: $score",
                    color = Color(0xFF69F0AE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("¡JUGAR DE NUEVO!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    onRetry: () -> Unit,
    onExitMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1318))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💥 ¡TE ATRAPARON!",
                    color = Color(0xFFFF5252),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Los granjeros casi te llevan al caldero... ¡No te rindas! Puedes saltar sobre sus cabezas o lanzarles objetos para defenderte.",
                    color = Color(0xFFECEFF1),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("retry_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🔄 REINTENTAR AHORA", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExitMenu,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Menú Principal", color = Color.White)
                }
            }
        }
    }
}
