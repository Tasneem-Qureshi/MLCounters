package com.mlcounters.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.ui.components.HeroCard
import com.mlcounters.app.ui.components.TierBadge
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.CyberPurple
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.ErrorRed
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getRoleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val dataLastUpdated by viewModel.dataLastUpdated.collectAsState()
    val recentDetections by viewModel.recentDetections.collectAsState()
    val topCounters by viewModel.topCounters.collectAsState()
    val heroCount by viewModel.totalHeroCount.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ML COUNTERS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = TextLightGray
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextLightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepBlack
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Prominent Start/Stop Overlay Button
            OverlayToggleButton(
                isActive = isOverlayActive,
                onToggle = { viewModel.toggleOverlay() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Card
            QuickStatsCard(
                heroCount = if (heroCount > 0) heroCount else 125,
                version = "v1.0.0 (Patch S34)",
                lastUpdated = dataLastUpdated
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Detections Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOverlayActive) Color(0xFF00E676) else TextMutedGray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECENT DETECTIONS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextWhite
                    )
                }

                Text(
                    text = if (isOverlayActive) "Overlay Active" else "Overlay Idle",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverlayActive) Color(0xFF00E676) else TextMutedGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentDetections.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkGraySurface,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No heroes detected yet. Launch overlay or game to detect enemy draft.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recentDetections.forEach { hero ->
                        Box(modifier = Modifier.width(110.dp)) {
                            HeroCard(
                                hero = hero,
                                onClick = { onNavigateToHeroDetail(hero.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Popular Counters Section
            Text(
                text = "POPULAR COUNTERS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (topCounters.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkGraySurface,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Analyzing meta counter data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topCounters.forEach { (targetHero, counterList) ->
                        CounterPairCard(
                            targetHero = targetHero,
                            counterHeroes = counterList,
                            onTargetHeroClick = { onNavigateToHeroDetail(targetHero.id) },
                            onCounterHeroClick = { heroId -> onNavigateToHeroDetail(heroId) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OverlayToggleButton(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val buttonGradient = if (isActive) {
        Brush.horizontalGradient(listOf(ErrorRed, Color(0xFFB71C1C)))
    } else {
        Brush.horizontalGradient(listOf(ElectricBlue, CyberPurple))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(buttonGradient)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Stop else Icons.Default.Layers,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isActive) "STOP FLOATING OVERLAY" else "START FLOATING OVERLAY",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = TextWhite
            )
        }
    }
}

@Composable
fun QuickStatsCard(
    heroCount: Int,
    version: String,
    lastUpdated: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "DATABASE STATUS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextMutedGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Heroes", value = "$heroCount", tint = ElectricBlue)
                StatItem(label = "Patch", value = version, tint = CyberPurple)
                StatItem(label = "Synced", value = lastUpdated.substringAfterLast(", "), tint = NeonOrange)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, tint: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextLightGray
        )
    }
}

@Composable
fun CounterPairCard(
    targetHero: HeroEntity,
    counterHeroes: List<HeroEntity>,
    onTargetHeroClick: () -> Unit,
    onCounterHeroClick: (Int) -> Unit
) {
    val targetRoleColor = getRoleColor(targetHero.role)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Target Hero (Left)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTargetHeroClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, targetRoleColor, CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (targetHero.portraitUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(targetHero.portraitUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = targetHero.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = targetHero.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = targetHero.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = targetRoleColor
                    )
                }
            }

            // VS indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "countered by",
                    tint = NeonOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "COUNTER",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    color = NeonOrange
                )
            }

            // Counter Heroes (Right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                counterHeroes.take(3).forEach { counter ->
                    val cRoleColor = getRoleColor(counter.role)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, cRoleColor, CircleShape)
                            .background(SurfaceVariantDark)
                            .clickable { onCounterHeroClick(counter.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (counter.portraitUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(counter.portraitUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = counter.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
