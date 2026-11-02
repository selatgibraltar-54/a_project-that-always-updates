package com.apksafe.cardgame.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apksafe.cardgame.data.model.GamePhase
import com.apksafe.cardgame.ui.components.AvatarCircle
import com.apksafe.cardgame.ui.components.BattlefieldCardView
import com.apksafe.cardgame.ui.components.HandCardView
import com.apksafe.cardgame.ui.components.PlayerHud
import com.apksafe.cardgame.ui.components.AfkTimerWidget
import com.apksafe.cardgame.ui.theme.AfkCritical
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import com.apksafe.cardgame.viewmodel.GameViewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameState by gameViewModel.gameState.collectAsState()
    val selectedHandCard by gameViewModel.selectedHandCard.collectAsState()
    val selectedAttacker by gameViewModel.selectedAttacker.collectAsState()

    var showSurrenderConfirm by remember { mutableStateOf(false) }

    val state = gameState ?: return

    // Haptic feedback function
    fun vibrateOnAfk() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        } catch (_: Exception) {}
    }

    // Trigger vibration when AFK notice fires
    LaunchedEffect(state.afkNotice) {
        if (state.afkNotice != null) {
            vibrateOnAfk()
        }
    }

    BackHandler {
        showSurrenderConfirm = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("game_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP ARENA BAR: Surrender, Round Indicator, Log Snippet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showSurrenderConfirm = true },
                    modifier = Modifier.testTag("surrender_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Menyerah",
                        tint = HealthRed.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Putaran ${state.roundNumber}",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Recent action ticker
                Text(
                    text = state.logs.lastOrNull() ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textAlign = TextAlign.End
                )
            }

            // OPPONENT ZONE: HUD & FIELD
            Column(modifier = Modifier.fillMaxWidth()) {
                val opponent = state.waitingPlayer
                PlayerHud(
                    player = opponent,
                    isActiveTurn = false,
                    isOpponent = true,
                    onTargetHeroClick = {
                        if (selectedAttacker != null) {
                            gameViewModel.attackWithSelected(null)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Opponent Battlefield Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    if (opponent.field.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Arena Musuh Kosong",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            items(opponent.field) { card ->
                                val hasOpponentTaunt = opponent.field.any { it.hasTaunt }
                                val isEligibleTarget = selectedAttacker != null && (!hasOpponentTaunt || card.hasTaunt)
                                BattlefieldCardView(
                                    card = card,
                                    isFriendly = false,
                                    isSelectedForAttack = false,
                                    isTargetable = isEligibleTarget,
                                    onClick = {
                                        if (selectedAttacker != null) {
                                            gameViewModel.attackWithSelected(card)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // CENTER BATTLEFIELD: AFK TIMER WIDGET & AFK ALERT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live AFK 4-minute timer widget
                AfkTimerWidget(
                    timeRemainingSeconds = state.afkTimeRemainingSeconds,
                    totalDurationSeconds = state.afkTotalDurationSeconds,
                    isFastTestMode = state.isFastTestMode,
                    activePlayerName = state.activePlayer.name,
                    onToggleSpeedMode = { gameViewModel.toggleFastAfkTestMode() },
                    onResetActivity = { gameViewModel.onUserActivity() }
                )

                // AFK Notification Banner (switched due to 4min timeout)
                AnimatedVisibility(
                    visible = state.afkNotice != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    state.afkNotice?.let { notice ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clickable { gameViewModel.clearNotice() }
                                .testTag("afk_notice_banner"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = AfkCritical.copy(alpha = 0.9f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = notice,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Tutup",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // PLAYER ZONE: FIELD, HUD, AND HAND
            Column(modifier = Modifier.fillMaxWidth()) {
                val active = state.activePlayer

                // Player Battlefield Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                ) {
                    if (active.field.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Arena Anda Kosong (Mainkan kartu dari tangan)",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            items(active.field) { card ->
                                val isSelected = selectedAttacker?.id == card.id
                                BattlefieldCardView(
                                    card = card,
                                    isFriendly = true,
                                    isSelectedForAttack = isSelected,
                                    isTargetable = false,
                                    onClick = {
                                        if (card.canAttack) {
                                            if (isSelected) {
                                                gameViewModel.selectAttacker(null)
                                            } else {
                                                gameViewModel.selectAttacker(card)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Player HUD
                PlayerHud(
                    player = active,
                    isActiveTurn = true,
                    isOpponent = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Selected Card Button
                    Button(
                        onClick = { gameViewModel.playSelectedCard() },
                        enabled = selectedHandCard != null && active.energy >= (selectedHandCard?.cost ?: 99),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("play_card_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedHandCard != null) "Mainkan (${selectedHandCard?.cost}E)" else "Pilih Kartu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Direct Attack Button (if attacker selected)
                    if (selectedAttacker != null) {
                        Button(
                            onClick = { gameViewModel.attackWithSelected(null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("direct_attack_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsKabaddi,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Serang Hero Musuh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // End Turn Button
                    Button(
                        onClick = { gameViewModel.endTurn() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("end_turn_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Akhiri Giliran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Hand Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .testTag("hand_cards_row")
                ) {
                    items(active.hand) { card ->
                        val isSelected = selectedHandCard?.id == card.id
                        val canAfford = active.energy >= card.cost
                        HandCardView(
                            card = card,
                            isSelected = isSelected,
                            canAfford = canAfford,
                            onClick = {
                                if (isSelected) {
                                    gameViewModel.selectHandCard(null)
                                } else {
                                    gameViewModel.selectHandCard(card)
                                }
                            }
                        )
                    }
                }
            }
        }

        // PASS DEVICE OVERLAY (FOR LOCAL MULTIPLAYER PASS & PLAY)
        if (state.phase == GamePhase.PASSING_TURN) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("pass_device_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarCircle(avatarId = state.activePlayer.avatar, size = 68.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "GILIRAN BERGANTI!",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Oper perangkat kepada:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.activePlayer.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Kartu tangan disembunyikan sampai pemain baru siap untuk menjaga kerahasiaan strategi.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { gameViewModel.dismissPassDeviceScreen() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("pass_device_ready_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Saya Siap! Mulai Giliran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // GAME OVER DIALOG
        if (state.phase == GamePhase.GAME_OVER) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("game_over_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = GoldPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "PERTANDINGAN SELESAI!",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pemenang: ${state.winnerName}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Alasan: ${state.gameOverReason ?: "KO"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // Special message when human user wins (not bot)
                        if (!state.isWinnerBot) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("anti_gambling_message_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "✨ PESAN KEMENANGAN ✨",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "ini lebih seru dari pada judi mainini aja ok?",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rewards card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.3f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hadiah Pertandingan Telah Disimpan ke Akun:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+50 Koin Emas • +40 XP (Pemenang)",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onBackToLobby,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("back_to_lobby_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Kembali ke Lobi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Surrender confirmation dialog
        if (showSurrenderConfirm) {
            AlertDialog(
                onDismissRequest = { showSurrenderConfirm = false },
                title = { Text("Menyerah dari Pertandingan?") },
                text = { Text("Pertandingan akan berakhir dan kemenangan diberikan kepada lawan.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSurrenderConfirm = false
                            gameViewModel.surrender()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
                    ) {
                        Text("Ya, Menyerah")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showSurrenderConfirm = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
