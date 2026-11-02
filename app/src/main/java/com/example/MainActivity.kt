package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.audio.GameAudioEngine
import com.example.game.engine.GameEngine
import com.example.game.ui.GameScreen
import com.example.game.ui.MainMenuScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    MAIN_MENU,
    PLAYING
}

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: GameAudioEngine
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        audioEngine = GameAudioEngine()
        gameEngine = GameEngine(audioEngine = audioEngine, vibrator = vibrator)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_MENU) }

                    when (currentScreen) {
                        ScreenState.MAIN_MENU -> {
                            MainMenuScreen(
                                engine = gameEngine,
                                onStartGame = {
                                    gameEngine.loadLevel(1)
                                    currentScreen = ScreenState.PLAYING
                                },
                                onSelectLevel = { levelId ->
                                    gameEngine.loadLevel(levelId)
                                    currentScreen = ScreenState.PLAYING
                                }
                            )
                        }
                        ScreenState.PLAYING -> {
                            GameScreen(
                                engine = gameEngine,
                                onBackToMenu = {
                                    audioEngine.currentTheme = GameAudioEngine.BgmTheme.PEACEFUL_FARM
                                    currentScreen = ScreenState.MAIN_MENU
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}
