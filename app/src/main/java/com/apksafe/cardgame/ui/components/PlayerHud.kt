package com.apksafe.cardgame.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.data.model.Player
import com.apksafe.cardgame.ui.theme.EnergyBlue
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.ShieldBlue
import com.apksafe.cardgame.ui.theme.SurfaceCard

@Composable
fun PlayerHud(
    player: Player,
    isActiveTurn: Boolean,
    isOpponent: Boolean,
    onTargetHeroClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hpFraction = (player.health.toFloat() / player.maxHealth.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isOpponent) "opponent_hud" else "player_hud"),
        color = if (isActiveTurn) SurfaceCard else SurfaceCard.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isActiveTurn) GoldPrimary else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(
                    avatarId = player.avatar,
                    size = 38.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isActiveTurn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldPrimary)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GILIRAN",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Hand & Deck info
                    Text(
                        text = "Kartu di tangan: ${player.hand.size} | Deck: ${player.deck.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Right: Health & Energy Indicators
            Column(horizontalAlignment = Alignment.End) {
                // Health & Shield Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = onTargetHeroClick != null) {
                        onTargetHeroClick?.invoke()
                    }
                ) {
                    // Shield
                    if (player.shield > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ShieldBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Shield",
                                tint = ShieldBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${player.shield}",
                                color = ShieldBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // HP Icon & Text
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Health",
                        tint = HealthRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${player.health}/${player.maxHealth}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // HP Bar
                LinearProgressIndicator(
                    progress = { hpFraction },
                    modifier = Modifier
                        .width(100.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = HealthRed,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Energy Crystals
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Energy",
                        tint = EnergyBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${player.energy}/${player.maxEnergy}",
                        color = EnergyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Crystal dots
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..player.maxEnergy) {
                            val isFilled = i <= player.energy
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) EnergyBlue else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
        }
    }
}
