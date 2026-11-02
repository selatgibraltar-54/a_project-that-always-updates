package com.apksafe.cardgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apksafe.cardgame.ui.theme.FireColor
import com.apksafe.cardgame.ui.theme.GoldPrimary
import com.apksafe.cardgame.ui.theme.LightningColor
import com.apksafe.cardgame.ui.theme.NatureColor
import com.apksafe.cardgame.ui.theme.VoidColor
import com.apksafe.cardgame.ui.theme.WaterColor

data class AvatarOption(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object AvatarCatalog {
    val options: List<AvatarOption> = listOf(
        AvatarOption("dragon", "Naga Api", Icons.Filled.LocalFireDepartment, FireColor),
        AvatarOption("phoenix", "Phoenix", Icons.Filled.ElectricBolt, LightningColor),
        AvatarOption("knight", "Kesatria", Icons.Filled.Shield, GoldPrimary),
        AvatarOption("wizard", "Penyihir", Icons.Filled.Psychology, VoidColor),
        AvatarOption("guardian", "Pelindung", Icons.Filled.Security, NatureColor),
        AvatarOption("siren", "Dewa Air", Icons.Filled.WaterDrop, WaterColor)
    )

    fun getOption(id: String): AvatarOption {
        return options.firstOrNull { it.id == id } ?: options.first()
    }
}

@Composable
fun AvatarCircle(
    avatarId: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val avatar = AvatarCatalog.getOption(avatarId)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatar.color.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = avatar.icon,
            contentDescription = avatar.name,
            tint = avatar.color,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
