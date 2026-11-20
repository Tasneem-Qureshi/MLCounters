package com.mlcounters.app.ui.screens.counter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mlcounters.app.util.ImageUtils
import com.mlcounters.app.ui.components.RoleChipRow
import com.mlcounters.app.ui.components.SearchBar
import com.mlcounters.app.ui.components.TierBadge
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.CyberPurple
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.MasteryExpert
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getCounterScoreColor
import com.mlcounters.app.ui.theme.getRoleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CounterViewModel = hiltViewModel()
) {
    val allHeroes by viewModel.allHeroes.collectAsState()
    val selectedHero by viewModel.selectedHero.collectAsState()
    val counters by viewModel.counters.collectAsState()
    val strongAgainst by viewModel.strongAgainst.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showOnlyMastered by viewModel.showOnlyMastered.collectAsState()

    var heroDropdownExpanded by remember { mutableStateOf(false) }
    var selectedSplitTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "COUNTER MATRIX",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = TextWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selected Hero Selector Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { heroDropdownExpanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val roleColor = selectedHero?.let { getRoleColor(it.role) } ?: ElectricBlue
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, roleColor, CircleShape)
                                .background(SurfaceVariantDark),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedHero?.portraitUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(ImageUtils.resolveHeroPortraitUrl(LocalContext.current, selectedHero!!.portraitUrl))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = selectedHero?.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextLightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "ANALYZE HERO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricBlue
                            )
                            Text(
                                text = selectedHero?.name ?: "Select Hero",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedHero != null) {
                            TierBadge(tier = selectedHero!!.tier)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Hero",
                            tint = ElectricBlue
                        )
                    }
                }

                DropdownMenu(
                    expanded = heroDropdownExpanded,
                    onDismissRequest = { heroDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(DarkGraySurface)
                ) {
                    allHeroes.forEach { h ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = h.name, color = TextWhite, fontWeight = FontWeight.SemiBold)
                                    Text(text = h.role, color = getRoleColor(h.role), style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                viewModel.selectHero(h.id)
                                heroDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Mastered Only Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (showOnlyMastered) MasteryExpert else TextMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show only my mastered heroes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLightGray
                    )
                }

                Switch(
                    checked = showOnlyMastered,
                    onCheckedChange = { viewModel.toggleMasteredFilter() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextWhite,
                        checkedTrackColor = MasteryExpert,
                        uncheckedTrackColor = SurfaceVariantDark,
                        uncheckedThumbColor = TextMutedGray
                    )
                )
            }

            // Search Bar for matrix
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearch(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = "Filter matchup heroes..."
            )

            // Role Filter Chips
            RoleChipRow(
                selectedRole = selectedRole,
                onRoleSelected = { viewModel.setRole(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Dual Tab / Split View Switcher
            TabRow(
                selectedTabIndex = selectedSplitTabIndex,
                containerColor = SurfaceVariantDark,
                contentColor = ElectricBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSplitTabIndex]),
                        color = if (selectedSplitTabIndex == 0) NeonOrange else Color(0xFF00E676)
                    )
                }
            ) {
                Tab(
                    selected = selectedSplitTabIndex == 0,
                    onClick = { selectedSplitTabIndex = 0 },
                    text = {
                        Text(
                            text = "COUNTERED BY (${counters.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSplitTabIndex == 0) NeonOrange else TextMutedGray
                        )
                    }
                )
                Tab(
                    selected = selectedSplitTabIndex == 1,
                    onClick = { selectedSplitTabIndex = 1 },
                    text = {
                        Text(
                            text = "STRONG AGAINST (${strongAgainst.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSplitTabIndex == 1) Color(0xFF00E676) else TextMutedGray
                        )
                    }
                )
            }

            // Split View Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val list = if (selectedSplitTabIndex == 0) counters else strongAgainst
                val isAdvantage = selectedSplitTabIndex == 1

                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showOnlyMastered)
                                "No mastered heroes match this criteria. Try turning off 'mastered heroes' filter."
                            else "No matchups found matching the active filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(list, key = { "${it.first.id}_$selectedSplitTabIndex" }) { (hero, score) ->
                            ScoredHeroItem(
                                hero = hero,
                                score = score,
                                isAdvantage = isAdvantage,
                                onClick = { onNavigateToHeroDetail(hero.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoredHeroItem(
    hero: HeroEntity,
    score: Int,
    isAdvantage: Boolean,
    onClick: () -> Unit
) {
    val roleColor = getRoleColor(hero.role)
    val scoreColor = getCounterScoreColor(score)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, roleColor, CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (hero.portraitUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(ImageUtils.resolveHeroPortraitUrl(LocalContext.current, hero.portraitUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = hero.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextLightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hero.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TierBadge(tier = hero.tier)
                    }
                    Text(
                        text = hero.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = scoreColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, scoreColor)
            ) {
                Text(
                    text = "Score: $score/10",
                    color = scoreColor,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
