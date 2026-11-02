package com.apksafe.cardgame.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.ui.components.AvatarCircle
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: com.apksafe.cardgame.viewmodel.AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()
    val matchHistory by authViewModel.matchHistory.collectAsState()
    val user = uiState.currentUser ?: return

    val totalMatches = user.matchesPlayed
    val winRate = if (totalMatches > 0) ((user.wins.toFloat() / totalMatches.toFloat()) * 100).toInt() else 0

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(avatarId = user.avatar, size = 52.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Akun Lokal Terverifikasi",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = AfkNormal
                                )
                            }
                        }

                        // Logout button
                        OutlinedButton(
                            onClick = { authViewModel.logout() },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthRed.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Keluar",
                                tint = HealthRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Keluar", color = HealthRed, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            title = "Menang",
                            value = "${user.wins}",
                            color = AfkNormal,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Kalah",
                            value = "${user.losses}",
                            color = HealthRed,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Win Rate",
                            value = "$winRate%",
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Koin",
                            value = "${user.coins}",
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Switch to Another Local Account
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ganti Akun Cepat di Perangkat Ini:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allUsers) { otherUser ->
                            val isCurrent = otherUser.id == user.id
                            Surface(
                                onClick = {
                                    if (!isCurrent) {
                                        authViewModel.quickSelectUser(otherUser)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) GoldPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) GoldPrimary else Color.Transparent
                                ),
                                modifier = Modifier.testTag("quick_user_${otherUser.username}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(avatarId = otherUser.avatar, size = 26.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = otherUser.username,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (isCurrent) "Aktif" else "Klik ganti",
                                            color = if (isCurrent) GoldPrimary else Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Add new account button
                        item {
                            Surface(
                                onClick = {
                                    authViewModel.logout()
                                    authViewModel.toggleMode(true)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.testTag("add_account_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Buat Akun",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Akun Baru",
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Match History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Riwayat Pertandingan Lokal",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Match History Items
        if (matchHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat pertandingan. Mulai main di arena!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(matchHistory) { match ->
                val isWinner = match.winnerName == user.username
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isWinner) AfkNormal.copy(alpha = 0.4f) else HealthRed.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isWinner) "🏆 Kemenangan" else "❌ Kekalahan",
                                    color = if (isWinner) AfkNormal else HealthRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${match.player1Name} vs ${match.player2Name})",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${match.roundsPlayed} Putaran • ${match.afkSwitchesCount}x Auto-Switch AFK • Alasan: ${match.endedReason}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = dateFormat.format(Date(match.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}
