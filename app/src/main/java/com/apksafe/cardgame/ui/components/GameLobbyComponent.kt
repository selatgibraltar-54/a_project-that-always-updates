package com.apksafe.cardgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiBad
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.data.network.ConnectionStatus
import com.apksafe.cardgame.data.network.DeviceUtils
import com.apksafe.cardgame.data.network.NetworkMonitor
import com.apksafe.cardgame.ui.theme.AfkCritical
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.ShieldBlue
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Game Lobby UI component using Compose that:
 * 1. Shows current account status (level, XP progress, coins, win rate, local security)
 * 2. Checks active WiFi or Mobile Data connection
 * 3. Enforces rule: Cannot use an account created on the same device
 * 4. Lists & discovers available players who are NOT on this device
 * 5. Provides a prominent 'Start Battle' button
 */
@Composable
fun GameLobbyComponent(
    currentUser: UserEntity,
    availablePlayers: List<UserEntity>,
    selectedOpponent: UserEntity?,
    isBotSelected: Boolean,
    onSelectOpponent: (UserEntity) -> Unit,
    onSelectBot: () -> Unit,
    onStartBattle: (p1: UserEntity, p2: UserEntity?, isBot: Boolean) -> Unit,
    onSwitchAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val thisDeviceId = remember { DeviceUtils.getThisDeviceId(context) }

    // Observe WiFi / Mobile Data
    var connectionStatus by remember { mutableStateOf(NetworkMonitor.getCurrentStatus(context)) }
    var isSearchingExternalPlayers by remember { mutableStateOf(false) }
    var searchFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var sameDeviceWarning by remember { mutableStateOf<String?>(null) }

    val hasInternet = connectionStatus == ConnectionStatus.WIFI || connectionStatus == ConnectionStatus.MOBILE_DATA

    val totalMatches = currentUser.matchesPlayed
    val winRate = if (totalMatches > 0) ((currentUser.wins.toFloat() / totalMatches.toFloat()) * 100).toInt() else 0
    val xpInLevel = currentUser.xp % 100
    val xpProgress = (xpInLevel / 100f).coerceIn(0f, 1f)

    // Separate accounts into:
    // 1. External non-device users (allowed for multiplayer matchmaking)
    // 2. Same-device accounts (disallowed by rule)
    val externalPlayers = availablePlayers.filter {
        it.id != currentUser.id && (it.originDeviceId.isEmpty() || it.originDeviceId != thisDeviceId)
    }

    val sameDevicePlayers = availablePlayers.filter {
        it.id != currentUser.id && it.originDeviceId.isNotEmpty() && it.originDeviceId == thisDeviceId
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_lobby_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 0. NETWORK STATUS BANNER (WiFi / Mobile Data)
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("network_status_banner"),
            shape = RoundedCornerShape(12.dp),
            color = if (hasInternet) SurfaceDark else HealthRed.copy(alpha = 0.15f),
            border = BorderStroke(
                1.dp,
                if (hasInternet) AfkNormal.copy(alpha = 0.5f) else HealthRed.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = when (connectionStatus) {
                            ConnectionStatus.WIFI -> Icons.Filled.Wifi
                            ConnectionStatus.MOBILE_DATA -> Icons.Filled.CellTower
                            ConnectionStatus.OFFLINE -> Icons.Filled.SignalWifiBad
                        },
                        contentDescription = "Status Jaringan",
                        tint = if (hasInternet) AfkNormal else HealthRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (connectionStatus) {
                                ConnectionStatus.WIFI -> "Terhubung via WiFi (Online)"
                                ConnectionStatus.MOBILE_DATA -> "Terhubung via Data Seluler (Online)"
                                ConnectionStatus.OFFLINE -> "⚠️ Tidak Ada Koneksi WiFi / Data Seluler"
                            },
                            color = if (hasInternet) AfkNormal else HealthRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (hasInternet) "Siap mencari pemain dari luar perangkat ini" else "Mode ini butuh WiFi atau Data Seluler aktif",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Refresh network check
                IconButton(
                    onClick = {
                        connectionStatus = NetworkMonitor.getCurrentStatus(context)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Periksa Koneksi",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ==========================================
        // 1. CURRENT ACCOUNT STATUS CARD
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("current_account_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(GoldPrimary.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                    .padding(14.dp)
            ) {
                // Header: Profile, Name, Status Pill & Switch Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(avatarId = currentUser.avatar, size = 48.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AfkNormal.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(AfkNormal)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Siap Bertarung",
                                        color = AfkNormal,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Perangkat Saya: ${thisDeviceId.take(12)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Ganti Akun Button
                    Surface(
                        onClick = onSwitchAccount,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.testTag("lobby_switch_user_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Ganti Akun",
                                tint = GoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ganti",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level ${currentUser.level}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$xpInLevel / 100 XP",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldPrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Row: Coins, Wins, Losses, Win Rate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Koin",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentUser.coins}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Win / Loss
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "W: ${currentUser.wins}",
                            color = AfkNormal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "L: ${currentUser.losses}",
                            color = HealthRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Win Rate %
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MilitaryTech,
                            contentDescription = null,
                            tint = ShieldBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$winRate% Win Rate",
                            color = ShieldBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. DISCOVERY & AVAILABLE PLAYERS
        // ==========================================
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pemain Luar Perangkat (Network)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dilarang memakai akun dari perangkat yang sama",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = AfkWarning
                    )
                }

                // Search online button
                OutlinedButton(
                    onClick = {
                        if (!hasInternet) {
                            searchFeedbackMessage = "Nyalakan WiFi atau Data Seluler untuk memindai!"
                            return@OutlinedButton
                        }
                        scope.launch {
                            isSearchingExternalPlayers = true
                            searchFeedbackMessage = null
                            delay(1200)
                            isSearchingExternalPlayers = false
                            searchFeedbackMessage = "Berhasil menemukan ${externalPlayers.size} pemain di luar perangkat ini!"
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("scan_network_rivals_btn")
                ) {
                    if (isSearchingExternalPlayers) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = GoldPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pindai Lawan", fontSize = 11.sp, color = GoldPrimary)
                    }
                }
            }

            // Feedback / Alert messages
            searchFeedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = msg,
                    color = AfkNormal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            sameDeviceWarning?.let { warn ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = HealthRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HealthRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = HealthRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = warn, color = HealthRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Practice Bot
            AvailablePlayerItem(
                name = "AI Bot (Training Mode)",
                subtitle = "Latihan Mandiri • Server Bot",
                avatarId = "wizard",
                deviceLabel = "Server Cloud AI",
                isBot = true,
                isAllowed = true,
                isSelected = isBotSelected,
                onClick = {
                    sameDeviceWarning = null
                    onSelectBot()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Non-Device External Players
            if (externalPlayers.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tekan 'Pindai Lawan' untuk mencari pemain dari perangkat lain via WiFi / Data.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    externalPlayers.forEach { player ->
                        val isSelected = !isBotSelected && selectedOpponent?.id == player.id
                        AvailablePlayerItem(
                            name = player.username,
                            subtitle = "Level ${player.level} • Menang: ${player.wins} • ${player.coins} Koin",
                            avatarId = player.avatar,
                            deviceLabel = "Device: ${player.originDeviceId}",
                            isBot = false,
                            isAllowed = true,
                            isSelected = isSelected,
                            onClick = {
                                sameDeviceWarning = null
                                onSelectOpponent(player)
                            }
                        )
                    }
                }
            }

            // Same Device Accounts List (Greyed out / Disallowed by the requested rule)
            if (sameDevicePlayers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Akun di Perangkat Ini (Dilarang untuk Multiplayer):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sameDevicePlayers.forEach { sameDevPlayer ->
                        AvailablePlayerItem(
                            name = sameDevPlayer.username,
                            subtitle = "Akun Lokal Perangkat Ini",
                            avatarId = sameDevPlayer.avatar,
                            deviceLabel = "Perangkat Sama (Diblokir)",
                            isBot = false,
                            isAllowed = false,
                            isSelected = false,
                            onClick = {
                                sameDeviceWarning = "❌ Tidak diizinkan! Aturan mewajibkan mencari pemain dari luar perangkat ini, bukan akun dari perangkat yang sama."
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. MATCHUP PREVIEW & 'START BATTLE' BUTTON
        // ==========================================
        val opponentName = when {
            isBotSelected -> "AI Bot (Training)"
            selectedOpponent != null -> selectedOpponent.username
            else -> "Belum Dipilih"
        }

        val canStart = (isBotSelected || (hasInternet && selectedOpponent != null && selectedOpponent.originDeviceId != thisDeviceId))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, if (canStart) GoldPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Versus Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // P1 Side
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarCircle(avatarId = currentUser.avatar, size = 36.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Perangkat Anda",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp
                        )
                    }

                    // VS Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            color = GoldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }

                    // P2 Side
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarCircle(
                            avatarId = if (isBotSelected) "wizard" else (selectedOpponent?.avatar ?: "phoenix"),
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opponentName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isBotSelected) "Server Bot" else "Luar Perangkat",
                            color = if (isBotSelected) ShieldBlue else AfkNormal,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // THE PROMINENT 'START BATTLE' BUTTON
                Button(
                    onClick = {
                        if (!isBotSelected && !hasInternet) {
                            sameDeviceWarning = "Mode Multiplayer membutuhkan koneksi WiFi atau Data Seluler aktif!"
                            return@Button
                        }
                        if (!isBotSelected && selectedOpponent == null) {
                            sameDeviceWarning = "Pilih pemain dari luar perangkat terlebih dahulu!"
                            return@Button
                        }
                        onStartBattle(
                            currentUser,
                            if (isBotSelected) null else selectedOpponent,
                            isBotSelected
                        )
                    },
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_battle_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsKabaddi,
                        contentDescription = "Mulai Bertanding",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!hasInternet && !isBotSelected) "BUTUH WIFI / DATA SELULER" else "START BATTLE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

                if (!hasInternet && !isBotSelected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aktifkan WiFi atau Data Seluler untuk memulai pertarungan pemain luar perangkat",
                        color = HealthRed,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailablePlayerItem(
    name: String,
    subtitle: String,
    avatarId: String,
    deviceLabel: String,
    isBot: Boolean,
    isAllowed: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            !isAllowed -> HealthRed.copy(alpha = 0.3f)
            isSelected -> GoldPrimary
            else -> Color.White.copy(alpha = 0.1f)
        },
        label = "border_color"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            !isAllowed -> Color.White.copy(alpha = 0.03f)
            isSelected -> GoldPrimary.copy(alpha = 0.15f)
            else -> SurfaceDark
        },
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("available_player_${name.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isBot) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ShieldBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = ShieldBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    AvatarCircle(avatarId = avatarId, size = 38.dp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = if (isAllowed) Color.White else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        !isAllowed -> HealthRed.copy(alpha = 0.2f)
                                        isBot -> ShieldBlue.copy(alpha = 0.2f)
                                        else -> AfkNormal.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (!isAllowed) "BLOCKED" else if (isBot) "BOT" else "ONLINE",
                                color = if (!isAllowed) HealthRed else if (isBot) ShieldBlue else AfkNormal,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$subtitle • $deviceLabel",
                        color = if (isAllowed) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp
                    )
                }
            }

            // Radio/Selection Icon or Blocked Icon
            if (!isAllowed) {
                Icon(
                    imageVector = Icons.Filled.Block,
                    contentDescription = "Dilarang",
                    tint = HealthRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GoldPrimary else Color.Transparent)
                        .border(1.5.dp, if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Terpilih",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
