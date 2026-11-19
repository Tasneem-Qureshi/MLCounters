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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getCounterScoreColor
import com.mlcounters.app.ui.theme.getRoleColor

@Composable
fun CounterCard(
    counterHero: HeroEntity,
    counterScore: Int,
    reason: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdvantage: Boolean = true // true = this hero beats the other; false = other beats this
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )
    val scoreColor = getCounterScoreColor(counterScore)
    val roleColor = getRoleColor(counterHero.role)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                verticalAlignment = Alignment.CenterVertizontally,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Hero Avatar + Name & Role
                Row(
                    verticalAlignment = Alignment.CenterVertizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, roleColor, CircleShape)
                            .background(SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (counterHero.portraitUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(counterHero.portraitUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = counterHero.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = counterHero.name,
                                tint = TextLightGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertizontally) {
                            Text(
                                text = counterHero.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TierBadge(tier = counterHero.tier)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = roleColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = counterHero.role,
                                color = roleColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right: Counter Score Badge & Expand Button
                Row(verticalAlignment = Alignment.CenterVertizontally) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scoreColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, scoreColor)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$counterScore/10",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = scoreColor
                            )
                            Text(
                                text = if (counterScore >= 8) "Strong" else if (counterScore >= 5) "Moderate" else "Situational",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = scoreColor
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

            Spacer(modifier = Modifier.height(8.dp))

            // Score progress bar
            LinearProgressIndicator(
                progress = { counterScore / 10f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = scoreColor,
                trackColor = SurfaceVariantDark,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reason Text (collapsed: 2 lines, expanded: full)
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLightGray,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

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
                    Text(
                        text = if (isAdvantage) "Matchup Advantage Tips:" else "Survival & Counterplay Tips:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = scoreColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Position carefully to capitalize on range and cooldown timings.\n• Coordinate with teammates when engaging this matchup.\n• Adjust item build according to enemy power spikes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedGray
                    )
                }
            }
        }
    }
}
