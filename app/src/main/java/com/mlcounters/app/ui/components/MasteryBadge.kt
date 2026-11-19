package com.mlcounters.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.getMasteryColor

fun getMasteryName(level: Int): String {
    return when (level) {
        1 -> "Beginner"
        2 -> "Intermediate"
        3 -> "Expert"
        4 -> "Master"
        else -> "Not Played"
    }
}

fun getMasteryIcon(level: Int): ImageVector {
    return when (level) {
        1 -> Icons.Default.MilitaryTech
        2 -> Icons.Default.Star
        3 -> Icons.Default.EmojiEvents
        4 -> Icons.Default.WorkspacePremium
        else -> Icons.Default.RadioButtonUnchecked
    }
}

@Composable
fun MasteryBadge(
    level: Int,
    modifier: Modifier = Modifier,
    clickable: Boolean = true,
    isCompact: Boolean = false,
    onLevelSelected: ((Int) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val color = getMasteryColor(level)
    val name = getMasteryName(level)
    val icon = getMasteryIcon(level)

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(if (isCompact) 4.dp else 8.dp),
            color = color.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
            modifier = Modifier.then(
                if (clickable && onLevelSelected != null) {
                    Modifier.clickable { expanded = true }
                } else Modifier
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertizontally,
                modifier = Modifier.padding(
                    horizontal = if (isCompact) 4.dp else 8.dp,
                    vertical = if (isCompact) 2.dp else 4.dp
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(if (isCompact) 12.dp else 16.dp)
                )

                if (!isCompact) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = name,
                        color = color,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                if (clickable && onLevelSelected != null && !isCompact) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Mastery",
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (clickable && onLevelSelected != null) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkGraySurface)
            ) {
                listOf(0, 1, 2, 3, 4).forEach { lvl ->
                    val itemColor = getMasteryColor(lvl)
                    val itemName = getMasteryName(lvl)
                    val itemIcon = getMasteryIcon(lvl)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertizontally) {
                                Icon(
                                    imageVector = itemIcon,
                                    contentDescription = null,
                                    tint = itemColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = itemName,
                                    color = itemColor,
                                    fontWeight = if (lvl == level) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onLevelSelected(lvl)
                        }
                    )
                }
            }
        }
    }
}
