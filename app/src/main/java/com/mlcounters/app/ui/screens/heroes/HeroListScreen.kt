package com.mlcounters.app.ui.screens.heroes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mlcounters.app.ui.components.HeroCard
import com.mlcounters.app.ui.components.RoleChipRow
import com.mlcounters.app.ui.components.SearchBar
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroListScreen(
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeroViewModel = hiltViewModel()
) {
    val heroes by viewModel.heroes.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val masteryMap by viewModel.masteryMap.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HERO ROSTER (${heroes.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = TextWhite
                    )
                },
                actions = {
                    androidx.compose.material3.IconButton(
                        onClick = { viewModel.refreshData() }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Sync from Remote",
                            tint = ElectricBlue
                        )
                    }
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
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = "Search by hero name or role..."
            )

            // Role Filter Chips Row
            RoleChipRow(
                selectedRole = selectedRole,
                onRoleSelected = { viewModel.setRole(it) },
                modifier = Modifier.fillMaxWidth()
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (heroes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextMutedGray,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No heroes found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your role filter or search query.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedGray
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(heroes, key = { it.id }) { hero ->
                            HeroCard(
                                hero = hero,
                                masteryLevel = masteryMap[hero.id],
                                onClick = { onNavigateToHeroDetail(hero.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
