package com.mlcounters.app.ui.screens.heroes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mlcounters.app.ui.components.CounterCard
import com.mlcounters.app.ui.components.ItemCard
import com.mlcounters.app.ui.components.MasteryBadge
import com.mlcounters.app.ui.components.TierBadge
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getRoleColor
import com.mlcounters.app.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeroDetailViewModel = hiltViewModel()
) {
    val hero by viewModel.hero.collectAsState()
    val counters by viewModel.counters.collectAsState()
    val strongAgainst by viewModel.strongAgainst.collectAsState()
    val counterItems by viewModel.counterItems.collectAsState()
    val mastery by viewModel.mastery.collectAsState()
    val allHeroes by viewModel.allHeroes.collectAsState()
    val itemsMap by viewModel.itemsMap.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Countered By (${counters.size})",
        "Strong Against (${strongAgainst.size})",
        "Counter Items (${counterItems.size})"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = hero?.name ?: "HERO DETAIL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DeepBlack)
            )
        }
    ) { innerPadding ->
        if (hero == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricBlue)
            }
        } else {
            val currentHero = hero!!
            val roleColor = getRoleColor(currentHero.role)
            val heroesMap = remember(allHeroes) { allHeroes.associateBy { it.id } }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Hero Header Profile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGraySurface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Hero Portrait
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, roleColor, CircleShape)
                            .background(SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentHero.portraitUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(ImageUtils.resolveHeroPortraitUrl(LocalContext.current, currentHero.portraitUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = currentHero.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = currentHero.name,
                                tint = TextLightGray,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hero Name & Tier
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentHero.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TierBadge(tier = currentHero.tier)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Role Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, roleColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = currentHero.role.uppercase(),
                                color = roleColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (!currentHero.secondaryRole.isNullOrBlank()) {
                            val secColor = getRoleColor(currentHero.secondaryRole)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = secColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, secColor.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = currentHero.secondaryRole.uppercase(),
                                    color = secColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clickable Mastery Selector
                    MasteryBadge(
                        level = mastery?.level ?: 0,
                        clickable = true,
                        isCompact = false,
                        onLevelSelected = { newLevel ->
                            viewModel.setMastery(newLevel, mastery?.notes ?: "")
                        }
                    )
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SurfaceVariantDark,
                    contentColor = ElectricBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ElectricBlue
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (selectedTabIndex == index) ElectricBlue else TextMutedGray,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            // "Countered By" tab
                            val distinctCounters = remember(counters) { counters.distinctBy { it.counterHeroId } }
                            if (distinctCounters.isEmpty()) {
                                EmptyTabPlaceholder(message = "No specific counter heroes recorded for ${currentHero.name}.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(distinctCounters, key = { "c_${it.heroId}_${it.counterHeroId}" }) { counter ->
                                        val enemyHero = heroesMap[counter.counterHeroId]
                                        if (enemyHero != null) {
                                            CounterCard(
                                                counterHero = enemyHero,
                                                counterScore = counter.counterScore,
                                                reason = counter.reason,
                                                isAdvantage = false,
                                                onClick = { onNavigateToHeroDetail(enemyHero.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // "Strong Against" tab
                            val distinctStrong = remember(strongAgainst) { strongAgainst.distinctBy { it.heroId } }
                            if (distinctStrong.isEmpty()) {
                                EmptyTabPlaceholder(message = "${currentHero.name} has no notable hard counter advantages recorded.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(distinctStrong, key = { "s_${it.heroId}_${it.counterHeroId}" }) { strong ->
                                        val victimHero = heroesMap[strong.heroId]
                                        if (victimHero != null) {
                                            CounterCard(
                                                counterHero = victimHero,
                                                counterScore = strong.counterScore,
                                                reason = strong.reason,
                                                isAdvantage = true,
                                                onClick = { onNavigateToHeroDetail(victimHero.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // "Counter Items" tab
                            val distinctItems = remember(counterItems) { counterItems.distinctBy { it.itemId } }
                            if (distinctItems.isEmpty()) {
                                EmptyTabPlaceholder(message = "No specialized counter items assigned for ${currentHero.name}.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(distinctItems, key = { "ci_${it.againstHeroId}_${it.itemId}" }) { counterItem ->
                                        val item = itemsMap[counterItem.itemId]
                                        if (item != null) {
                                            ItemCard(
                                                item = item,
                                                priority = counterItem.priority,
                                                counterReason = counterItem.reason
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedGray
        )
    }
}
