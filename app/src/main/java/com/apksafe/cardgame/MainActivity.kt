package com.apksafe.cardgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.apksafe.cardgame.data.db.CardDatabase
import com.apksafe.cardgame.data.repository.UserRepository
import com.apksafe.cardgame.ui.screens.AuthScreen
import com.apksafe.cardgame.ui.screens.GameScreen
import com.apksafe.cardgame.ui.screens.LobbyScreen
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.CardTheme
import com.apksafe.cardgame.viewmodel.AuthViewModel
import com.apksafe.cardgame.viewmodel.GameViewModel

enum class AppScreen {
    AUTH,
    LOBBY,
    GAME
}

class MainActivity : ComponentActivity() {

    private val database by lazy { CardDatabase.getInstance(applicationContext) }
    private val userRepository by lazy { UserRepository(database) }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(userRepository)
    }

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModel.Factory(userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CardTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = BgDark
                ) {
                    MainAppContent(
                        authViewModel = authViewModel,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    authViewModel: AuthViewModel,
    gameViewModel: GameViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }

    // If user is logged out, go to AUTH
    if (authState.currentUser == null && currentScreen != AppScreen.AUTH) {
        currentScreen = AppScreen.AUTH
    }

    when (currentScreen) {
        AppScreen.AUTH -> {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    currentScreen = AppScreen.LOBBY
                }
            )
        }
        AppScreen.LOBBY -> {
            LobbyScreen(
                authViewModel = authViewModel,
                onStartGame = { p1, p2, isBot ->
                    gameViewModel.startMatch(p1, p2, isBot)
                    currentScreen = AppScreen.GAME
                },
                onSwitchAccount = {
                    authViewModel.logout()
                    currentScreen = AppScreen.AUTH
                }
            )
        }
        AppScreen.GAME -> {
            GameScreen(
                gameViewModel = gameViewModel,
                onBackToLobby = {
                    currentScreen = AppScreen.LOBBY
                }
            )
        }
    }
}
