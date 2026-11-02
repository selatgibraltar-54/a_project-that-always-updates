package com.apksafe.cardgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.data.network.ConnectionStatus
import com.apksafe.cardgame.data.network.DeviceUtils
import com.apksafe.cardgame.data.network.NetworkMonitor
import com.apksafe.cardgame.data.network.p2p.NetworkPlayer
import com.apksafe.cardgame.data.network.p2p.P2PConnectionState
import com.apksafe.cardgame.data.network.p2p.P2PNetworkManager
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.ShieldBlue
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val thisDeviceId = remember { DeviceUtils.getThisDeviceId(context) }
    val localIp = remember { DeviceUtils.getLocalIpAddress() }

    val p2pManager = remember { P2PNetworkManager.getInstance(context) }
    val discoveredNetworkPlayers by p2pManager.discoveredPlayers.collectAsState()
    val p2pConnectionState by p2pManager.connectionState.collectAsState()

    // Start network discovery on active screen
    DisposableEffect(currentUser) {
        p2pManager.startDiscovery(currentUser)
        onDispose {
            // keep discovery running or clean up
        }
    }

    // Live WiFi / Mobile Data
    var connectionStatus by remember { mutableStateOf(NetworkMonitor.getCurrentStatus(context)) }
    var isScanning by remember { mutableStateOf(false) }
    var scanFeedback by remember { mutableStateOf<String?>(null) }
    var sameDeviceWarning by remember { mutableStateOf<String?>(null) }

    // Direct IP join input
    var hostIpInput by remember { mutableStateOf("") }
    var isJoiningIp by remember { mutableStateOf(false) }

    val hasInternet = connectionStatus == ConnectionStatus.WIFI || connectionStatus == ConnectionStatus.MOBILE_DATA

    val totalMatches = currentUser.matchesPlayed
    val winRate = if (totalMatches > 0) ((currentUser.wins.toFloat() / totalMatches.toFloat()) * 100).toInt() else 0
    val xpInLevel = currentUser.xp % 100
    val xpProgress = (xpInLevel / 100f).coerceIn(0f, 1f)

    // Other accounts physically registered on this same device (Disallowed for battle)
    val sameDevicePlayers = availablePlayers.filter {
        it.id != currentUser.id && (it.originDeviceId.isEmpty() || it.originDeviceId == thisDeviceId)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_lobby_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 0. LIVE NETWORK STATUS (WIFI / MOBILE DATA / HOTSPOT)
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
                                ConnectionStatus.WIFI -> "Terhubung via WiFi (IP: $localIp)"
                                ConnectionStatus.MOBILE_DATA -> "Terhubung via Data Seluler / Hotspot"
                                ConnectionStatus.OFFLINE -> "⚠️ Offline (Nyalakan WiFi / Data / Hotspot)"
                            },
                            color = if (hasInternet) AfkNormal else HealthRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (hasInternet) "Multiplayer P2P LAN aktif (Tanpa Firebase)" else "Mode ini butuh WiFi atau Data Seluler aktif",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        connectionStatus = NetworkMonitor.getCurrentStatus(context)
                        p2pManager.broadcastBeacon(currentUser, isHost = false)
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
        // 1. CURRENT USER STATUS CARD
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
                                text = "Device ID: ${thisDeviceId.take(12)} • IP: $localIp",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

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

                Spacer(modifier = Modifier.height(10.dp))

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

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${currentUser.coins}", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "W: ${currentUser.wins}", color = AfkNormal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "L: ${currentUser.losses}", color = HealthRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "$winRate% Win Rate", color = ShieldBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // 2. DISCOVERED NETWORK PLAYERS (REAL P2P, NO FAKE ACCOUNTS, NO FIREBASE)
        // ==========================================
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pemain Luar Perangkat (Network P2P)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real peer via WiFi/Hotspot • Dilarang akun dari perangkat yang sama",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = AfkWarning
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (!hasInternet) {
                            scanFeedback = "Nyalakan WiFi / Hotspot / Data untuk memindai!"
                            return@OutlinedButton
                        }
                        scope.launch {
                            isScanning = true
                            scanFeedback = null
                            p2pManager.broadcastBeacon(currentUser, isHost = false)
                            delay(1200)
                            isScanning = false
                            scanFeedback = if (discoveredNetworkPlayers.isEmpty()) {
                                "Pencarian selesai. Belum ada HP lain yang terdeteksi di jaringan ini."
                            } else {
                                "Ditemukan ${discoveredNetworkPlayers.size} pemain di luar perangkat ini!"
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("scan_network_rivals_btn")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = GoldPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pindai Lawan", fontSize = 11.sp, color = GoldPrimary)
                    }
                }
            }

            scanFeedback?.let { msg ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = msg, color = if (discoveredNetworkPlayers.isNotEmpty()) AfkNormal else Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }

            sameDeviceWarning?.let { warn ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = HealthRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HealthRed)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = HealthRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = warn, color = HealthRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Practice Bot
            DiscoveredPlayerCard(
                name = "AI Bot (Training Mode)",
                subtitle = "Latihan Mandiri • Server Bot Offline",
                avatarId = "wizard",
                deviceBadge = "Solo Practice",
                isBot = true,
                isAllowed = true,
                isSelected = isBotSelected,
                onClick = {
                    sameDeviceWarning = null
                    onSelectBot()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // REAL DISCOVERED EXTERNAL PLAYERS
            if (discoveredNetworkPlayers.isEmpty()) {
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
                        Icon(Icons.Filled.Podcasts, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Belum ada pemain lain yang terdeteksi di jaringan WiFi / Hotspot ini.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buka game 'Card' di HP lain dalam jaringan yang sama untuk deteksi otomatis, atau gunakan 'Host Room' di bawah!",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    discoveredNetworkPlayers.forEach { player ->
                        val isSelected = !isBotSelected && selectedOpponent?.username == player.username
                        DiscoveredPlayerCard(
                            name = player.username,
                            subtitle = "Level ${player.level} • Menang: ${player.wins} • IP: ${player.ipAddress}",
                            avatarId = player.avatar,
                            deviceBadge = if (player.isHost) "HOST ROOM" else "ONLINE (LAN)",
                            isBot = false,
                            isAllowed = true,
                            isSelected = isSelected,
                            onClick = {
                                sameDeviceWarning = null
                                onSelectOpponent(player.toUserEntity())
                            }
                        )
                    }
                }
            }

            // SAME DEVICE ACCOUNTS (BLOCKED)
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
                        DiscoveredPlayerCard(
                            name = sameDevPlayer.username,
                            subtitle = "Akun lokal pada perangkat yang sama",
                            avatarId = sameDevPlayer.avatar,
                            deviceBadge = "SAME DEVICE (BLOCKED)",
                            isBot = false,
                            isAllowed = false,
                            isSelected = false,
                            onClick = {
                                sameDeviceWarning = "❌ Dilarang! Tidak boleh bertanding menggunakan akun dari perangkat yang sama. Hubungkan perangkat lain lewat WiFi / Hotspot."
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. HOST ROOM & DIRECT IP CONNECT (NO FIREBASE)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Router, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Koneksi Langsung (Host & Gabung Room via IP)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = p2pConnectionState) {
                    is P2PConnectionState.Hosting -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AfkNormal.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AfkNormal)
                        ) {
                            Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AfkNormal, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Room Aktif! Menunggu lawan terhubung...",
                                        color = AfkNormal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bagikan IP ke lawan: ${state.localIp}:${state.port}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { p2pManager.disconnect() },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HealthRed)
                                ) {
                                    Text("Batalkan Host Room", color = HealthRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    is P2PConnectionState.Connected -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AfkNormal.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AfkNormal)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = AfkNormal, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Terhubung dengan ${state.opponent.username}!",
                                        color = AfkNormal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Lawan di IP: ${state.opponent.ipAddress} (Beda Perangkat)",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Host Room Button
                            Button(
                                onClick = {
                                    p2pManager.hostRoom(currentUser) { opp ->
                                        onSelectOpponent(opp.toUserEntity())
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue)
                            ) {
                                Text("Buat Room (Host)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Join via IP Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = hostIpInput,
                                onValueChange = { hostIpInput = it.trim() },
                                placeholder = { Text("Ketik IP Host (misal 192.168.1.5)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShieldBlue,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                                )
                            )

                            Button(
                                onClick = {
                                    if (hostIpInput.isNotEmpty()) {
                                        keyboardController?.hide()
                                        isJoiningIp = true
                                        p2pManager.joinRoom(
                                            currentUser = currentUser,
                                            hostIp = hostIpInput,
                                            onConnected = { opp ->
                                                isJoiningIp = false
                                                onSelectOpponent(opp.toUserEntity())
                                            },
                                            onError = { err ->
                                                isJoiningIp = false
                                                sameDeviceWarning = err
                                            }
                                        )
                                    }
                                },
                                enabled = hostIpInput.isNotEmpty() && !isJoiningIp,
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                            ) {
                                if (isJoiningIp) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text("Gabung", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. MATCHUP PREVIEW & 'START BATTLE' BUTTON
        // ==========================================
        val opponentName = when {
            isBotSelected -> "AI Bot (Training)"
            selectedOpponent != null -> selectedOpponent.username
            else -> "Belum Dipilih"
        }

        val canStart = isBotSelected || (hasInternet && selectedOpponent != null && selectedOpponent.originDeviceId != thisDeviceId)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // P1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarCircle(avatarId = currentUser.avatar, size = 36.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = currentUser.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Perangkat Anda", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "VS", color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    // P2
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarCircle(
                            avatarId = if (isBotSelected) "wizard" else (selectedOpponent?.avatar ?: "phoenix"),
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = opponentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = if (isBotSelected) "Solo Bot" else "Luar Perangkat (P2P)",
                            color = if (isBotSelected) ShieldBlue else AfkNormal,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                    Icon(imageVector = Icons.Filled.SportsKabaddi, contentDescription = "Mulai Bertanding", modifier = Modifier.size(20.dp))
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
private fun DiscoveredPlayerCard(
    name: String,
    subtitle: String,
    avatarId: String,
    deviceBadge: String,
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
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isBot) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(ShieldBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SmartToy, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(20.dp))
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
                                text = deviceBadge,
                                color = if (!isAllowed) HealthRed else if (isBot) ShieldBlue else AfkNormal,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = if (isAllowed) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp
                    )
                }
            }

            if (!isAllowed) {
                Icon(Icons.Filled.Block, contentDescription = "Dilarang", tint = HealthRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
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
                        Icon(Icons.Filled.Check, contentDescription = "Terpilih", tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
