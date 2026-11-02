package com.apksafe.cardgame.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.ui.components.GameLobbyComponent
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import com.apksafe.cardgame.viewmodel.AuthViewModel

enum class LobbyNavTab {
    ARENA,
    CODEX,
    HISTORY
}

@Composable
fun LobbyScreen(
    authViewModel: AuthViewModel,
    onStartGame: (p1: UserEntity, p2: UserEntity?, isBot: Boolean) -> Unit,
    onSwitchAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()
    val currentUser = uiState.currentUser ?: return

    var currentTab by remember { mutableStateOf(LobbyNavTab.ARENA) }
    var isBotSelected by remember { mutableStateOf(false) }
    var selectedPlayer2 by remember {
        mutableStateOf<UserEntity?>(null)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("lobby_screen"),
        containerColor = BgDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == LobbyNavTab.ARENA,
                    onClick = { currentTab = LobbyNavTab.ARENA },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Lobby Arena") },
                    label = { Text("Game Lobby") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == LobbyNavTab.CODEX,
                    onClick = { currentTab = LobbyNavTab.CODEX },
                    icon = { Icon(Icons.Filled.Book, contentDescription = "Codex") },
                    label = { Text("Koleksi Kartu") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == LobbyNavTab.HISTORY,
                    onClick = { currentTab = LobbyNavTab.HISTORY },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Profil & Riwayat") },
                    label = { Text("Profil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            LobbyNavTab.ARENA -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // AFK 4-Minute Rule Explainer Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("afk_rule_info_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, AfkWarning.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AfkWarning.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AvTimer,
                                        contentDescription = null,
                                        tint = AfkWarning,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Aturan Auto-Switch AFK 4 Menit",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Jika pemain tidak aktif selama lebih dari 4 menit (240 detik), giliran secara otomatis dialihkan ke pemain lawan! Tersedia tombol demo kilat (10s) di arena.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Game Lobby UI Component (shows account status, lists available players, Start Battle button)
                    item {
                        GameLobbyComponent(
                            currentUser = currentUser,
                            availablePlayers = allUsers,
                            selectedOpponent = selectedPlayer2,
                            isBotSelected = isBotSelected,
                            onSelectOpponent = { opponent ->
                                selectedPlayer2 = opponent
                                isBotSelected = false
                            },
                            onSelectBot = {
                                isBotSelected = true
                            },
                            onStartBattle = { p1, p2, isBot ->
                                onStartGame(p1, p2, isBot)
                            },
                            onSwitchAccount = onSwitchAccount
                        )
                    }
                }
            }

            LobbyNavTab.CODEX -> {
                CodexScreen(modifier = Modifier.padding(paddingValues))
            }

            LobbyNavTab.HISTORY -> {
                ProfileScreen(
                    authViewModel = authViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
