package com.mlcounters.app.ui.screens.mastery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.ui.components.HeroCard
import com.mlcounters.app.ui.components.getMasteryName
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.MasteryBeginner
import com.mlcounters.app.ui.theme.MasteryExpert
import com.mlcounters.app.ui.theme.MasteryIntermediate
import com.mlcounters.app.ui.theme.MasteryMaster
import com.mlcounters.app.ui.theme.MasteryNotPlayed
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import com.mlcounters.app.ui.theme.getMasteryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasteryScreen(
    onNavigateToHeroDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MasteryViewModel = hiltViewModel()
) {
    val allHeroes by viewModel.allHeroes.collectAsState()
    val filteredHeroes by viewModel.filteredHeroes.collectAsState()
    val masteryMap by viewModel.masteryMap.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var editingHero by remember { mutableStateOf<HeroEntity?>(null) }

    // Overall mastery calculation
    val totalHeroes = allHeroes.size
    val totalMasteryScore = masteryMap.values.sumOf { it.level }
    val maxPossibleScore = (if (totalHeroes > 0) totalHeroes else 1) * 4
    val masteryProgress = (totalMasteryScore.toFloat() / maxPossibleScore).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HERO MASTERY TRACKER",
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
            // Overall Mastery Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MasteryExpert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OVERALL MASTERY PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                        Text(
                            text = "${(masteryProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MasteryExpert
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { masteryProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MasteryExpert,
                        trackColor = SurfaceVariantDark,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Stats Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MasteryStatChip(title = "Unplayed", count = stats.notPlayed, color = MasteryNotPlayed)
                MasteryStatChip(title = "Beginner", count = stats.beginner, color = MasteryBeginner)
                MasteryStatChip(title = "Intermediate", count = stats.intermediate, color = MasteryIntermediate)
                MasteryStatChip(title = "Expert", count = stats.expert, color = MasteryExpert)
                MasteryStatChip(title = "Master", count = stats.master, color = MasteryMaster)
            }

            // Level Filter Chips Row
            val levelFilterOptions = listOf(
                null to "All",
                0 to "Unplayed",
                1 to "Beginner",
                2 to "Intermediate",
                3 to "Expert",
                4 to "Master"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levelFilterOptions.forEach { (lvl, label) ->
                    val isSelected = selectedLevel == lvl
                    val chipColor = if (lvl == null) ElectricBlue else getMasteryColor(lvl)

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setLevelFilter(lvl) },
                        label = { Text(text = label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkGraySurface,
                            labelColor = TextLightGray,
                            selectedContainerColor = chipColor,
                            selectedLabelColor = if (lvl == 3 || lvl == 4) DeepBlack else TextWhite
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

            // Grid of Heroes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (filteredHeroes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No heroes found with this mastery level.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedGray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredHeroes, key = { it.id }) { hero ->
                            HeroCard(
                                hero = hero,
                                masteryLevel = masteryMap[hero.id]?.level ?: 0,
                                onClick = {
                                    editingHero = hero
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Mastery Dialog
    editingHero?.let { hero ->
        val currentMastery = masteryMap[hero.id]
        EditMasteryDialog(
            hero = hero,
            initialLevel = currentMastery?.level ?: 0,
            initialNotes = currentMastery?.notes ?: "",
            onDismiss = { editingHero = null },
            onSave = { level, notes ->
                viewModel.setMastery(hero.id, level, notes)
                editingHero = null
            },
            onViewDetails = {
                editingHero = null
                onNavigateToHeroDetail(hero.id)
            }
        )
    }
}

@Composable
fun MasteryStatChip(title: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMutedGray
            )
        }
    }
}

@Composable
fun EditMasteryDialog(
    hero: HeroEntity,
    initialLevel: Int,
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit,
    onViewDetails: () -> Unit
) {
    var selectedLevel by remember { mutableIntStateOf(initialLevel) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGraySurface,
        title = {
            Text(
                text = "Edit Mastery: ${hero.name}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select your proficiency tier:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextLightGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                listOf(0, 1, 2, 3, 4).forEach { lvl ->
                    val color = getMasteryColor(lvl)
                    val name = getMasteryName(lvl)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLevel = lvl }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = color,
                                unselectedColor = TextMutedGray
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            color = if (selectedLevel == lvl) color else TextLightGray,
                            fontWeight = if (selectedLevel == lvl) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Strategy Notes", color = TextMutedGray) },
                    placeholder = { Text("Combos, counters, matchup notes...", color = TextMutedGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLightGray,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onViewDetails,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "View Full Matchups →", color = ElectricBlue)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedLevel, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Save", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextLightGray)
            }
        }
    )
}
