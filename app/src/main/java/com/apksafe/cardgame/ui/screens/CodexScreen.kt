package com.apksafe.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.apksafe.cardgame.ui.components.getElementCardImageRes
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.apksafe.cardgame.data.model.CardCatalog
import com.apksafe.cardgame.data.model.CardElement
import com.apksafe.cardgame.ui.components.getElementIcon
import com.apksafe.cardgame.ui.theme.BgDark
import com.apksafe.cardgame.ui.theme.EnergyBlue
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.HealthRed
import com.apksafe.cardgame.ui.theme.ShieldBlue
import com.apksafe.cardgame.ui.theme.SurfaceCard
import com.apksafe.cardgame.ui.theme.SurfaceDark

@Composable
fun CodexScreen(modifier: Modifier = Modifier) {
    var selectedElementFilter by remember { mutableStateOf<CardElement?>(null) }

    val cards = if (selectedElementFilter == null) {
        CardCatalog.allCards
    } else {
        CardCatalog.allCards.filter { it.element == selectedElementFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp)
            .testTag("codex_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Koleksi & Kodeks Kartu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total ${CardCatalog.allCards.size} kartu pertarungan fantasi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Element filter tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterPill(
                    label = "Semua",
                    isSelected = selectedElementFilter == null,
                    color = GoldPrimary,
                    onClick = { selectedElementFilter = null }
                )
            }
            items(CardElement.values()) { element ->
                FilterPill(
                    label = element.label,
                    isSelected = selectedElementFilter == element,
                    color = Color(element.colorHex),
                    onClick = { selectedElementFilter = element }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cards list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cards) { card ->
                val elemColor = Color(card.element.colorHex)
                val rarityColor = Color(card.rarity.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("codex_item_${card.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, elemColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Mana Cost Badge & Element Icon
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EnergyBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${card.cost}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = getElementIcon(card.element),
                                contentDescription = card.element.label,
                                tint = elemColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Card Art Preview
                        Box(
                            modifier = Modifier
                                .size(54.dp, 44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, elemColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        ) {
                            Image(
                                painter = painterResource(id = getElementCardImageRes(card.element)),
                                contentDescription = card.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Middle: Name, Type, Description
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${card.rarity.label}",
                                    color = rarityColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "${card.type.label} (${card.element.label})",
                                style = MaterialTheme.typography.labelSmall,
                                color = elemColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = card.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: Attack & Defense
                        Column(horizontalAlignment = Alignment.End) {
                            if (card.attack > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${card.attack}",
                                        color = HealthRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "ATK", color = HealthRed, fontSize = 9.sp)
                                }
                            }
                            if (card.defense > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${card.defense}",
                                        color = ShieldBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "HP", color = ShieldBlue, fontSize = 9.sp)
                                }
                            }
                            if (card.hasTaunt) {
                                Text(
                                    text = "TAUNT",
                                    color = ShieldBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color else Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color.Transparent)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
