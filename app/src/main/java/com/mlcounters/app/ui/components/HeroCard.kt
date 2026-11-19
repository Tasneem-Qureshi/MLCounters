package com.mlcounters.app.ui.components

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.CyberPurple
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getRoleColor

@Composable
fun HeroCard(
    hero: HeroEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    masteryLevel: Int? = null,
    isCompact: Boolean = false
) {
    if (isCompact) {
        CompactHeroCard(
            hero = hero,
            onClick = onClick,
            modifier = modifier,
            masteryLevel = masteryLevel
        )
    } else {
        StandardHeroCard(
            hero = hero,
            onClick = onClick,
            modifier = modifier,
            masteryLevel = masteryLevel
        )
    }
}

@Composable
fun StandardHeroCard(
    hero: HeroEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    masteryLevel: Int? = null
) {
    val roleColor = getRoleColor(hero.role)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Portrait with circular clip, role border, and Tier badge
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(2.dp, roleColor, CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (hero.portraitUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(hero.portraitUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = hero.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = hero.name,
                            tint = TextLightGray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Tier Badge
                TierBadge(tier = hero.tier)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hero Name
            Text(
                text = hero.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Role Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = roleColor.copy(alpha = 0.2f),
                border = BorderStroke(0.5.dp, roleColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = hero.role.uppercase(),
                    color = roleColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Optional Mastery Badge
            if (masteryLevel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                MasteryBadge(
                    level = masteryLevel,
                    clickable = false,
                    isCompact = true
                )
            }
        }
    }
}

@Composable
fun CompactHeroCard(
    hero: HeroEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    masteryLevel: Int? = null
) {
    val roleColor = getRoleColor(hero.role)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Portrait
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, roleColor, CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (hero.portraitUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(hero.portraitUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = hero.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = hero.name,
                            tint = TextLightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = hero.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = hero.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TierBadge(tier = hero.tier)
                if (masteryLevel != null) {
                    MasteryBadge(
                        level = masteryLevel,
                        clickable = false,
                        isCompact = true
                    )
                }
            }
        }
    }
}

@Composable
fun TierBadge(tier: String) {
    val (bgColor, textColor) = when (tier.uppercase()) {
        "S" -> NeonOrange to Color.Black
        "A" -> CyberPurple to TextWhite
        "B" -> ElectricBlue to TextWhite
        else -> DarkGraySurface to TextLightGray
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = tier.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
            color = textColor
        )
    }
}
