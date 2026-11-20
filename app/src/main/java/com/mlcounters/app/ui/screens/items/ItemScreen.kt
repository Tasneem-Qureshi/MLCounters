package com.mlcounters.app.ui.screens.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.mlcounters.app.ui.components.ItemCard
import com.mlcounters.app.ui.components.SearchBar
import com.mlcounters.app.ui.components.getItemTypeColor
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getRoleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedHero by viewModel.selectedHero.collectAsState()
    val counterItems by viewModel.counterItems.collectAsState()
    val allHeroes by viewModel.allHeroes.collectAsState()

    var heroDropdownExpanded by remember { mutableStateOf(false) }
    val itemTypes = listOf("All", "Physical", "Magical", "Defense", "Movement")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ITEM COUNTER GUIDE",
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
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearch(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = "Search items, passives, stats..."
            )

            // "Counter items for:" hero selector card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { heroDropdownExpanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
                border = BorderStroke(1.dp, if (selectedHero != null) NeonOrange else BorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedHero != null) {
                            val roleColor = getRoleColor(selectedHero!!.role)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, roleColor, CircleShape)
                                    .background(SurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedHero!!.portraitUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(selectedHero!!.portraitUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = selectedHero!!.name,
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
                                    text = "Counter items against:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonOrange
                                )
                                Text(
                                    text = selectedHero!!.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Counter items for hero:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedGray
                                )
                                Text(
                                    text = "Select enemy hero to show counters",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextLightGray
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedHero != null) {
                            IconButton(
                                onClick = { viewModel.selectHeroForCounterItems(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selection",
                                    tint = TextMutedGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Hero",
                            tint = if (selectedHero != null) NeonOrange else ElectricBlue
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
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Show All Items (Clear selection)",
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            viewModel.selectHeroForCounterItems(null)
                            heroDropdownExpanded = false
                        }
                    )

                    allHeroes.forEach { h ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = h.name, color = TextWhite)
                                    Text(text = h.role, color = getRoleColor(h.role), style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                viewModel.selectHeroForCounterItems(h.id)
                                heroDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Type Filter Chips Row
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemTypes.forEach { type ->
                    val isSelected = if (type == "All") {
                        selectedType == null || selectedType.equals("All", ignoreCase = true)
                    } else {
                        selectedType.equals(type, ignoreCase = true)
                    }

                    val chipColor = if (type == "All") ElectricBlue else getItemTypeColor(type)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setType(if (type == "All" || isSelected) null else type)
                        },
                        label = { Text(text = type) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkGraySurface,
                            labelColor = TextLightGray,
                            selectedContainerColor = chipColor,
                            selectedLabelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderDark,
                            selectedBorderColor = chipColor
                        )
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedHero != null) {
                    // Counter items mode
                    if (counterItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recommended counter items configured against ${selectedHero!!.name}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedGray
                            )
                        }
                    } else {
                        val itemsMap = remember(items) { items.associateBy { it.id } }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonOrange.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, NeonOrange.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Top recommended items against ${selectedHero!!.name}:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeonOrange,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            val distinctCI = counterItems.distinctBy { it.itemId }
                            items(distinctCI, key = { "ci_${it.againstHeroId}_${it.itemId}" }) { ci ->
                                val itemEntity = itemsMap[ci.itemId]
                                if (itemEntity != null) {
                                    ItemCard(
                                        item = itemEntity,
                                        priority = ci.priority,
                                        counterReason = ci.reason
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // All items browsing mode
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items found matching the selected filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedGray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                ItemCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}
