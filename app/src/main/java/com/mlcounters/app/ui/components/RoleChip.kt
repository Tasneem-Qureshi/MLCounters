package com.mlcounters.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getRoleColor

fun getRoleIcon(role: String): ImageVector {
    return when (role.trim().lowercase()) {
        "tank" -> Icons.Default.Shield
        "fighter" -> Icons.Default.SportsMma
        "assassin" -> Icons.Default.ElectricBolt
        "mage" -> Icons.Default.AutoFixHigh
        "marksman" -> Icons.Default.GpsFixed
        "support" -> Icons.Default.Healing
        else -> Icons.Default.GridView
    }
}

@Composable
fun RoleChip(
    roleName: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roleColor = if (roleName.equals("All", ignoreCase = true)) ElectricBlue else getRoleColor(roleName)
    val roleIcon = getRoleIcon(roleName)

    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = roleName,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = roleIcon,
                contentDescription = roleName,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) TextWhite else roleColor
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = DarkGraySurface,
            labelColor = TextLightGray,
            selectedContainerColor = roleColor,
            selectedLabelColor = TextWhite
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = BorderDark,
            selectedBorderColor = roleColor
        )
    )
}

@Composable
fun RoleChipRow(
    selectedRole: String?,
    onRoleSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        roles.forEach { role ->
            val isSelected = if (role == "All") {
                selectedRole == null || selectedRole.equals("All", ignoreCase = true)
            } else {
                selectedRole.equals(role, ignoreCase = true)
            }

            RoleChip(
                roleName = role,
                isSelected = isSelected,
                onSelected = {
                    if (role == "All") {
                        onRoleSelected(null)
                    } else {
                        onRoleSelected(if (isSelected) null else role)
                    }
                }
            )
        }
    }
}
