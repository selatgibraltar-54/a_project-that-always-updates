package com.apksafe.cardgame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.db.MatchHistoryEntity
import com.apksafe.cardgame.ui.components.AvatarCircle
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.EnergyBlue
import com.apksafe.cardgame.ui.theme.GoldAccent
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceCardBorder
import com.apksafe.cardgame.ui.theme.SurfaceDark
import com.apksafe.cardgame.ui.theme.VoidColor
import com.apksafe.cardgame.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MatchHistoryFilter {
    ALL,
    WINS,
    LOSSES
}

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateToArena: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    val allHistory by authViewModel.matchHistory.collectAsState()
    val user = uiState.currentUser ?: return

    // Filter matches relevant to the logged-in user
    val userMatches = remember(allHistory, user.username) {
        allHistory.filter { it.player1Name == user.username || it.player2Name == user.username }
    }

    // Win / Loss / Stats calculation
    val wins = user.wins
    val losses = user.losses
    val totalMatches = wins + losses
    val winRate = if (totalMatches > 0) (wins.toFloat() / totalMatches.toFloat() * 100f) else 0f
    val winLossRatio = if (losses > 0) {
        String.format(Locale.US, "%.2f", wins.toFloat() / losses.toFloat())
    } else if (wins > 0) {
        "MAX"
    } else {
        "0.00"
    }

    // Ending reason breakdown
    val koCount = userMatches.count { it.endedReason == "KO" }
    val afkCount = userMatches.count { it.endedReason == "AFK_FORFEIT" }
    val surrenderCount = userMatches.count { it.endedReason == "SURRENDER" }
    val otherEndCount = (userMatches.size - (koCount + afkCount + surrenderCount)).coerceAtLeast(0)

    // AFK switches total across all matches
    val totalAfkSwitches = userMatches.sumOf { it.afkSwitchesCount }
    val avgRounds = if (userMatches.isNotEmpty()) {
        String.format(Locale.US, "%.1f", userMatches.map { it.roundsPlayed }.average())
    } else {
        "0.0"
    }

    // Selected filter for match list
    var selectedFilter by remember { mutableStateOf(MatchHistoryFilter.ALL) }

    val filteredMatches = remember(userMatches, selectedFilter, user.username) {
        when (selectedFilter) {
            MatchHistoryFilter.ALL -> userMatches
            MatchHistoryFilter.WINS -> userMatches.filter { it.winnerName == user.username }
            MatchHistoryFilter.LOSSES -> userMatches.filter { it.winnerName != user.username }
        }
    }

    // Form streak (last matches)
    val recentForm = remember(userMatches, user.username) {
        userMatches.take(8).map { it.winnerName == user.username }
    }

    val currentStreak = remember(userMatches, user.username) {
        if (userMatches.isEmpty()) return@remember "0"
        val isFirstWin = userMatches.first().winnerName == user.username
        var count = 0
        for (m in userMatches) {
            val won = m.winnerName == user.username
            if (won == isFirstWin) count++ else break
        }
        if (isFirstWin) "🔥 $count Win Streak" else "💀 $count Loss Streak"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header with Engine Info
        item {
            DashboardHeaderCard(
                username = user.username,
                avatar = user.avatar,
                level = user.level,
                coins = user.coins,
                xp = user.xp
            )
        }

        // Quick simulation tools for interactive review
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_quick_actions"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulasi Interaktif DataViz",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { authViewModel.recordDemoMatch(isWin = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = AfkNormal.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_sim_win")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Win", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { authViewModel.recordDemoMatch(isWin = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthRed.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_sim_loss")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Loss", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // KPI Scorecards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Win Rate",
                    value = String.format(Locale.US, "%.1f%%", winRate),
                    subtitle = "$wins Menang / $losses Kalah",
                    accentColor = if (winRate >= 50f) AfkNormal else HealthRed,
                    modifier = Modifier.weight(1f),
                    tag = "kpi_win_rate"
                )
                KpiCard(
                    title = "W/L Ratio",
                    value = "${winLossRatio}x",
                    subtitle = currentStreak,
                    accentColor = GoldPrimary,
                    modifier = Modifier.weight(1f),
                    tag = "kpi_wl_ratio"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Total Match",
                    value = "$totalMatches",
                    subtitle = "Avg: $avgRounds Ronde",
                    accentColor = EnergyBlue,
                    modifier = Modifier.weight(1f),
                    tag = "kpi_total_matches"
                )
                KpiCard(
                    title = "AFK Switch (4m)",
                    value = "${totalAfkSwitches}x",
                    subtitle = "Auto-Pindah Giliran",
                    accentColor = AfkWarning,
                    modifier = Modifier.weight(1f),
                    tag = "kpi_afk_switches"
                )
            }
        }

        // Native Donut Chart (Recharts-style PieChart equivalent)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_win_loss_donut"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Visualisasi Rasio Win / Loss",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Recharts Style",
                            color = GoldAccent.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalMatches == 0) {
                        EmptyChartPlaceholder(onNavigateToArena = onNavigateToArena)
                    } else {
                        WinLossDonutChart(
                            wins = wins,
                            losses = losses,
                            winRate = winRate
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ratio Proportional Bar
                        WinLossRatioBar(
                            wins = wins,
                            losses = losses,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Victory Ending Reasons Bar Chart (Recharts-style BarChart equivalent)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_end_reasons_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Distribusi Kondisi Selesai Duel",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bagaimana duel diselesaikan (KO, batas waktu AFK 4 menit, dsb):",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (userMatches.isEmpty()) {
                        Text(
                            text = "Belum ada rekaman pertandingan untuk dianalisis.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val totalRecorded = userMatches.size.coerceAtLeast(1)
                        HorizontalStatBar(
                            label = "KO (Penghabisan HP)",
                            count = koCount,
                            total = totalRecorded,
                            color = HealthRed,
                            icon = Icons.Filled.HighlightOff
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalStatBar(
                            label = "AFK Forfeit (Aturan 4 Menit)",
                            count = afkCount,
                            total = totalRecorded,
                            color = AfkWarning,
                            icon = Icons.Filled.AvTimer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalStatBar(
                            label = "Surrender (Menyerah)",
                            count = surrenderCount,
                            total = totalRecorded,
                            color = VoidColor,
                            icon = Icons.Filled.Security
                        )
                    }
                }
            }
        }

        // Recent Form Streak Strip
        if (recentForm.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_form_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Timeline,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tren Performa (8 Match Terakhir)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = currentStreak,
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentForm.forEachIndexed { index, isWin ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isWin) AfkNormal.copy(alpha = 0.25f) else HealthRed.copy(alpha = 0.25f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isWin) AfkNormal else HealthRed,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isWin) "W" else "L",
                                        color = if (isWin) AfkNormal else HealthRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Match History Section Header & Filters
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Pertandingan Detail",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredMatches.size} Duel",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == MatchHistoryFilter.ALL,
                        onClick = { selectedFilter = MatchHistoryFilter.ALL },
                        label = { Text("Semua (${userMatches.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = SurfaceDark,
                            labelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )

                    FilterChip(
                        selected = selectedFilter == MatchHistoryFilter.WINS,
                        onClick = { selectedFilter = MatchHistoryFilter.WINS },
                        label = { Text("Menang ($wins)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AfkNormal,
                            selectedLabelColor = Color.Black,
                            containerColor = SurfaceDark,
                            labelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.testTag("filter_wins")
                    )

                    FilterChip(
                        selected = selectedFilter == MatchHistoryFilter.LOSSES,
                        onClick = { selectedFilter = MatchHistoryFilter.LOSSES },
                        label = { Text("Kalah ($losses)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HealthRed,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.testTag("filter_losses")
                    )
                }
            }
        }

        // Match History Items
        if (filteredMatches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_match_history_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Riwayat untuk Kategori Ini",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mainkan pertandingan di arena atau gunakan tombol simulasi di atas.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                MatchHistoryDetailCard(
                    match = match,
                    currentUsername = user.username
                )
            }
        }
    }
}

@Composable
private fun DashboardHeaderCard(
    username: String,
    avatar: String,
    level: Int,
    coins: Int,
    xp: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCircle(avatarId = avatar, size = 52.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "LVL $level",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$coins Koin • $xp XP",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tag: String
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WinLossDonutChart(
    wins: Int,
    losses: Int,
    winRate: Float
) {
    val total = (wins + losses).coerceAtLeast(1)
    val targetWinSweep = (wins.toFloat() / total.toFloat()) * 360f

    val animatedWinSweep by animateFloatAsState(
        targetValue = targetWinSweep,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "win_sweep"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Canvas Donut
        Box(
            modifier = Modifier
                .size(140.dp)
                .testTag("donut_chart_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Losses Arc (Red)
                drawArc(
                    color = HealthRed,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Wins Arc (Green)
                if (animatedWinSweep > 0f) {
                    drawArc(
                        color = AfkNormal,
                        startAngle = -90f,
                        sweepAngle = animatedWinSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Center Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%.0f%%", winRate),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "WIN RATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Recharts-style Legend & Metrics
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            LegendItem(
                color = AfkNormal,
                label = "Menang (Wins)",
                count = wins,
                percent = if (total > 0) (wins * 100 / total) else 0
            )

            LegendItem(
                color = HealthRed,
                label = "Kalah (Losses)",
                count = losses,
                percent = if (total > 0) (losses * 100 / total) else 0
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Total $total Pertandingan",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int,
    percent: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count Match ($percent%)",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun WinLossRatioBar(
    wins: Int,
    losses: Int,
    modifier: Modifier = Modifier
) {
    val total = (wins + losses).coerceAtLeast(1)
    val winFraction = wins.toFloat() / total.toFloat()

    val animatedFraction by animateFloatAsState(
        targetValue = winFraction,
        animationSpec = tween(durationMillis = 800),
        label = "ratio_bar"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Rasio Visual Bar",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
            Text(
                text = "${(winFraction * 100).toInt()}% W : ${( (1f - winFraction) * 100).toInt()}% L",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(HealthRed)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                    .background(AfkNormal)
            )
        }
    }
}

@Composable
private fun HorizontalStatBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val fraction = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 700),
        label = "stat_bar"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$count (${(fraction * 100).toInt()}%)",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(onNavigateToArena: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Insights,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Belum Ada Data Duel",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "Mainkan pertandingan pertamamu di Arena untuk mengaktifkan chart!",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToArena,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mulai Duel di Arena", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MatchHistoryDetailCard(
    match: MatchHistoryEntity,
    currentUsername: String
) {
    val isWinner = match.winnerName == currentUsername
    val opponentName = if (match.player1Name == currentUsername) match.player2Name else match.player1Name
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(match.timestamp) { dateFormat.format(Date(match.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp,
            if (isWinner) AfkNormal.copy(alpha = 0.4f) else HealthRed.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Outcome Icon Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWinner) AfkNormal.copy(alpha = 0.15f) else HealthRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWinner) Icons.Filled.CheckCircle else Icons.Filled.HighlightOff,
                        contentDescription = null,
                        tint = if (isWinner) AfkNormal else HealthRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isWinner) "MENANG" else "KALAH",
                            color = if (isWinner) AfkNormal else HealthRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "vs $opponentName",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${match.roundsPlayed} Ronde",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = when (match.endedReason) {
                                "KO" -> "Selesai: KO"
                                "AFK_FORFEIT" -> "Selesai: AFK 4m"
                                "SURRENDER" -> "Selesai: Menyerah"
                                else -> match.endedReason
                            },
                            color = GoldAccent.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        if (match.afkSwitchesCount > 0) {
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${match.afkSwitchesCount}x AFK Switch",
                                color = AfkWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Timestamp
            Text(
                text = formattedDate,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}
