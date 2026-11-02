package com.apksafe.cardgame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.ui.theme.AfkCritical
import com.apksafe.cardgame.ui.theme.AfkNormal
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.SurfaceCard

@Composable
fun AfkTimerWidget(
    timeRemainingSeconds: Int,
    totalDurationSeconds: Int,
    isFastTestMode: Boolean,
    activePlayerName: String,
    onToggleSpeedMode: () -> Unit,
    onResetActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (timeRemainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)

    val timerColor by animateColorAsState(
        targetValue = when {
            timeRemainingSeconds <= 30 -> AfkCritical
            timeRemainingSeconds <= 120 -> AfkWarning
            else -> AfkNormal
        },
        label = "timer_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "critical_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onResetActivity)
            .testTag("afk_timer_widget"),
        color = SurfaceCard,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (timeRemainingSeconds <= 30) AfkCritical.copy(alpha = blinkAlpha) else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and active player
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (timeRemainingSeconds <= 30) Icons.Filled.Warning else Icons.Filled.AvTimer,
                        contentDescription = "Timer AFK",
                        tint = timerColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Giliran: $activePlayerName",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isFastTestMode) "Batas AFK: 10s (Demo Cepat)" else "Batas AFK: 4 Menit (Auto-Switch)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Digital Countdown & Mode toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time text
                    Box(
                        modifier = Modifier
                            .background(timerColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, timerColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            color = timerColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Toggle fast mode button
                    Surface(
                        onClick = onToggleSpeedMode,
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFastTestMode) GoldPrimary else Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.testTag("toggle_afk_speed_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = if (isFastTestMode) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isFastTestMode) "10s" else "4m",
                                color = if (isFastTestMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = timerColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            if (timeRemainingSeconds <= 30) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ PERINGATAN: Dalam $timeRemainingSeconds detik giliran akan dialihkan ke pemain lain!",
                    color = AfkCritical,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
