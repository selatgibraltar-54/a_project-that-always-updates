package com.apksafe.cardgame.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apksafe.cardgame.R
import com.apksafe.cardgame.data.model.Card
import com.apksafe.cardgame.data.model.CardElement
import com.apksafe.cardgame.data.model.CardType
import com.apksafe.cardgame.ui.theme.AfkWarning
import com.apksafe.cardgame.ui.theme.EnergyBlue
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.ShieldBlue
import com.apksafe.cardgame.ui.theme.SurfaceCard

fun getElementIcon(element: CardElement): ImageVector {
    return when (element) {
        CardElement.FIRE -> Icons.Filled.LocalFireDepartment
        CardElement.WATER -> Icons.Filled.WaterDrop
        CardElement.NATURE -> Icons.Filled.Park
        CardElement.LIGHTNING -> Icons.Filled.ElectricBolt
        CardElement.VOID -> Icons.Filled.Shield
    }
}

fun getElementCardImageRes(element: CardElement): Int {
    return when (element) {
        CardElement.FIRE -> R.drawable.img_card_fire
        CardElement.WATER -> R.drawable.img_card_water
        CardElement.NATURE -> R.drawable.img_card_nature
        CardElement.LIGHTNING -> R.drawable.img_card_lightning
        CardElement.VOID -> R.drawable.img_card_void
    }
}

@Composable
fun HandCardView(
    card: Card,
    isSelected: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elementColor = Color(card.element.colorHex)
    val rarityColor = Color(card.rarity.colorHex)

    val borderStroke = when {
        isSelected -> BorderStroke(2.dp, GoldPrimary)
        canAfford -> BorderStroke(1.dp, elementColor.copy(alpha = 0.8f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    }

    Card(
        modifier = modifier
            .width(115.dp)
            .height(172.dp)
            .scale(if (isSelected) 1.05f else 1.0f)
            .clickable(onClick = onClick)
            .testTag("hand_card_${card.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) SurfaceCard else SurfaceCard.copy(alpha = 0.6f)
        ),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 3.dp)
    ) {
        Box {
            // Background subtle gradient
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                elementColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(5.dp)
                    .matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Mana cost, Title, and Element Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mana crystal
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(EnergyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${card.cost}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )

                    // Element icon
                    Icon(
                        imageVector = getElementIcon(card.element),
                        contentDescription = card.element.label,
                        tint = elementColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Card Artwork Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, elementColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = painterResource(id = getElementCardImageRes(card.element)),
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    // Rarity pill overlay on art
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = card.rarity.label,
                            color = rarityColor,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Description
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 8.5.sp, lineHeight = 10.5.sp),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Stats footer: Attack & Defense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.attack > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(HealthRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${card.attack}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }

                    if (card.hasTaunt) {
                        Text(
                            text = "TAUNT",
                            color = ShieldBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (card.defense > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ShieldBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${card.defense}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BattlefieldCardView(
    card: Card,
    isFriendly: Boolean,
    isSelectedForAttack: Boolean,
    isTargetable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elementColor = Color(card.element.colorHex)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val borderStroke = when {
        isSelectedForAttack -> BorderStroke(2.dp, GoldPrimary)
        isTargetable -> BorderStroke(2.dp, HealthRed)
        card.hasTaunt -> BorderStroke(2.dp, ShieldBlue)
        card.canAttack && isFriendly -> BorderStroke(1.5.dp, GoldPrimary.copy(alpha = glowAlpha))
        else -> BorderStroke(1.dp, elementColor.copy(alpha = 0.5f))
    }

    Surface(
        modifier = modifier
            .width(72.dp)
            .height(86.dp)
            .clickable(onClick = onClick)
            .testTag("field_card_${card.id}"),
        shape = RoundedCornerShape(8.dp),
        color = SurfaceCard,
        border = borderStroke,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(3.dp)
                .background(Brush.verticalGradient(listOf(elementColor.copy(alpha = 0.3f), Color.Transparent))),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getElementIcon(card.element),
                    contentDescription = null,
                    tint = elementColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    textAlign = TextAlign.Center
                )
                if (card.hasTaunt) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Taunt",
                        tint = ShieldBlue,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Artwork miniature
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Image(
                    painter = painterResource(id = getElementCardImageRes(card.element)),
                    contentDescription = card.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            // Attack & Current HP Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ATK
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(HealthRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${card.attack}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // HP
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (card.currentHp < card.defense) AfkWarning else ShieldBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${card.currentHp}",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
