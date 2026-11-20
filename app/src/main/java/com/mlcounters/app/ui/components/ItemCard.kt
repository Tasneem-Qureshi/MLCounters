package com.mlcounters.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mlcounters.app.data.local.entity.ItemEntity
import com.mlcounters.app.util.ImageUtils
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.CyberPurple
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.RoleTank
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite

fun getItemTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "physical" -> NeonOrange
        "magical" -> CyberPurple
        "defense" -> RoleTank
        "movement" -> ElectricBlue
        "roaming" -> Color(0xFF66BB6A)
        "jungling" -> Color(0xFFFFD54F)
        else -> ElectricBlue
    }
}

@Composable
fun ItemCard(
    item: ItemEntity,
    modifier: Modifier = Modifier,
    priority: Int? = null,
    counterReason: String? = null,
    onClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "itemArrowRotation"
    )
    val typeColor = getItemTypeColor(item.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Item Icon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, typeColor, RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.iconUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(ImageUtils.resolveItemIconUrl(LocalContext.current, item.iconUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = item.name,
                                tint = typeColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = typeColor.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, typeColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = item.type,
                                    color = typeColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (!item.subType.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SurfaceVariantDark
                                ) {
                                    Text(
                                        text = item.subType,
                                        color = TextMutedGray,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (priority != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonOrange.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NeonOrange)
                        ) {
                            Text(
                                text = "P$priority",
                                color = NeonOrange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = TextMutedGray,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            // Stats summary
            if (item.stats.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.stats,
                    style = MaterialTheme.typography.bodySmall.copy(color = ElectricBlue, fontWeight = FontWeight.SemiBold)
                )
            }

            // Passive or Counter Reason
            if (!counterReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Counter Reason: $counterReason",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextLightGray),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (item.passive.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.passive,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextLightGray),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Expanded details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(SurfaceVariantDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (item.description.isNotBlank()) {
                        Text(
                            text = "Item Overview:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = typeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightGray
                        )
                    }

                    if (!counterReason.isNullOrBlank() && item.passive.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Item Passive:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.passive,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightGray
                        )
                    }
                }
            }
        }
    }
}
