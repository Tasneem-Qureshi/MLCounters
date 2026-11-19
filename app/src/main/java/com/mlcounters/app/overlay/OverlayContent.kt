package com.mlcounters.app.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity

// Dark Gaming Color Palette
private val DarkBgBase = Color(0xFF0F121C)
private val DarkSurface = Color(0xFF171B2A)
private val DarkSurfaceElevated = Color(0xFF20263A)
private val AccentCyan = Color(0xFF00E5FF)
private val AccentPink = Color(0xFFFF2A6D)
private val MasterGold = Color(0xFFFFD700)
private val TextWhite = Color(0xFFF3F5FA)
private val TextMuted = Color(0xFF8E9BAE)
private val BorderMuted = Color(0xFF2C344A)

val ROLES_LIST = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")

@Composable
fun OverlayContent(
    viewModel: OverlayViewModel,
    onDragDelta: (Float, Float) -> Unit,
    onManualScanRequested: () -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    onCloseRequested: () -> Unit
) {
    val isExpanded by viewModel.isExpanded.collectAsState()
    val opacity by viewModel.overlayOpacity.collectAsState()

    if (!isExpanded) {
        CollapsedOverlayView(
            viewModel = viewModel,
            opacity = opacity,
            onDragDelta = onDragDelta,
            onExpandClicked = { viewModel.toggleExpanded() }
        )
    } else {
        ExpandedOverlayView(
            viewModel = viewModel,
            opacity = opacity,
            onDragDelta = onDragDelta,
            onManualScanRequested = onManualScanRequested,
            onSearchFocusChanged = onSearchFocusChanged,
            onCollapseClicked = { viewModel.toggleExpanded() },
            onCloseRequested = onCloseRequested
        )
    }
}

/**
 * Collapsed State: Small circular floating button with ML Counters badge and pulsating scan ring.
 */
@Composable
private fun CollapsedOverlayView(
    viewModel: OverlayViewModel,
    opacity: Float,
    onDragDelta: (Float, Float) -> Unit,
    onExpandClicked: () -> Unit
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val enemyHeroes by viewModel.allEnemyHeroes.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(62.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        DarkSurfaceElevated.copy(alpha = opacity),
                        DarkBgBase.copy(alpha = opacity)
                    )
                )
            )
            .border(
                width = if (isScanning) 2.5.dp else 1.5.dp,
                color = if (isScanning) AccentCyan.copy(alpha = pulseBorder) else AccentCyan.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable { onExpandClicked() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ML",
                color = AccentCyan,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = if (enemyHeroes.isNotEmpty()) "${enemyHeroes.size}/5" else "SCAN",
                color = if (enemyHeroes.isNotEmpty()) AccentPink else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = AccentCyan,
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Expanded State: Full floating card with detected heroes, counter picks, items, and filters.
 */
@Composable
private fun ExpandedOverlayView(
    viewModel: OverlayViewModel,
    opacity: Float,
    onDragDelta: (Float, Float) -> Unit,
    onManualScanRequested: () -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    onCollapseClicked: () -> Unit,
    onCloseRequested: () -> Unit
) {
    val detectedHeroes by viewModel.detectedHeroes.collectAsState()
    val manualHeroes by viewModel.manualHeroes.collectAsState()
    val allEnemies by viewModel.allEnemyHeroes.collectAsState()
    val counterPicks by viewModel.counterPicks.collectAsState()
    val counterItems by viewModel.counterItems.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val masteredHeroes by viewModel.masteredHeroes.collectAsState()
    val sensitivity by viewModel.detectionSensitivity.collectAsState()
    val allAvailableHeroes by viewModel.allAvailableHeroes.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showManualPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .width(350.dp)
            .heightIn(max = 540.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderMuted, RoundedCornerShape(16.dp)),
        color = DarkBgBase.copy(alpha = opacity),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Drag Handle & Header
            HeaderSection(
                isScanning = isScanning,
                showSettings = showSettings,
                onDragDelta = onDragDelta,
                onScanClicked = onManualScanRequested,
                onSettingsToggle = { showSettings = !showSettings },
                onCollapseClicked = onCollapseClicked,
                onCloseClicked = onCloseRequested
            )

            // Optional Settings Panel
            AnimatedVisibility(visible = showSettings) {
                SettingsPanel(
                    opacity = opacity,
                    sensitivity = sensitivity,
                    onOpacityChange = { viewModel.setOverlayOpacity(it) },
                    onSensitivityChange = { viewModel.setDetectionSensitivity(it) }
                )
            }

            // Scrollable main body
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Detected / Enemy Heroes Row
                EnemyHeroesSection(
                    allEnemies = allEnemies,
                    onRemoveEnemy = { id -> viewModel.removeEnemyHero(id) },
                    onOpenPicker = { showManualPicker = true },
                    onClearAll = { viewModel.clearAllEnemies() }
                )

                // Manual Hero Picker Dropdown / Sheet
                if (showManualPicker) {
                    ManualHeroPicker(
                        availableHeroes = allAvailableHeroes,
                        currentEnemyIds = allEnemies.map { it.id }.toSet(),
                        onHeroSelected = { hero ->
                            viewModel.addManualHero(hero)
                            showManualPicker = false
                            onSearchFocusChanged(false)
                        },
                        onDismiss = {
                            showManualPicker = false
                            onSearchFocusChanged(false)
                        },
                        onFocusChanged = onSearchFocusChanged
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Role Filter Pills
                RoleFilterBar(
                    selectedRole = selectedRole ?: "All",
                    onRoleSelected = { viewModel.setRole(it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Best Counters Section
                Text(
                    text = "BEST COUNTERS",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (allEnemies.isEmpty()) {
                    EmptyEnemiesPrompt(onManualScanRequested)
                } else {
                    allEnemies.forEach { enemy ->
                        val counters = counterPicks[enemy.id] ?: emptyList()
                        EnemyCounterCard(
                            enemy = enemy,
                            counters = counters,
                            masteredHeroIds = masteredHeroes
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Counter Items Section
                if (counterItems.isNotEmpty()) {
                    Text(
                        text = "COUNTER ITEMS",
                        color = AccentPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    counterItems.forEach { (item, reason) ->
                        CounterItemCard(item = item, reason = reason)
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top Draggable Header with Controls
 */
@Composable
private fun HeaderSection(
    isScanning: Boolean,
    showSettings: Boolean,
    onDragDelta: (Float, Float) -> Unit,
    onScanClicked: () -> Unit,
    onSettingsToggle: () -> Unit,
    onCollapseClicked: () -> Unit,
    onCloseClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Drag Indicator Bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BorderMuted)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ML COUNTERS",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isScanning) AccentCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isScanning) "SCANNING..." else "LIVE",
                        color = if (isScanning) AccentCyan else AccentPink,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Manual Scan Trigger Button
                IconButton(
                    onClick = onScanClicked,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.CameraAlt,
                        contentDescription = "Scan Screen",
                        tint = AccentCyan,
                        modifier = Modifier
                            .size(17.dp)
                            .then(if (isScanning) Modifier.rotate(spinAngle) else Modifier)
                    )
                }

                // Settings Toggle
                IconButton(
                    onClick = onSettingsToggle,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (showSettings) AccentCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Minimize Button
                IconButton(
                    onClick = onCollapseClicked,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Collapse",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onCloseClicked,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Overlay",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Settings Subpanel for Opacity and Detection Sensitivity
 */
@Composable
private fun SettingsPanel(
    opacity: Float,
    sensitivity: Float,
    onOpacityChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Overlay Opacity", color = TextWhite, fontSize = 11.sp)
            Text(text = "${(opacity * 100).toInt()}%", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = 0.3f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan,
                inactiveTrackColor = BorderMuted
            ),
            modifier = Modifier.height(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Detection Sensitivity", color = TextWhite, fontSize = 11.sp)
            Text(text = "${(sensitivity * 100).toInt()}%", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = sensitivity,
            onValueChange = onSensitivityChange,
            valueRange = 0.5f..0.95f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan,
                inactiveTrackColor = BorderMuted
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

/**
 * Section displaying detected and manual enemy heroes (max 5)
 */
@Composable
private fun EnemyHeroesSection(
    allEnemies: List<HeroEntity>,
    onRemoveEnemy: (Int) -> Unit,
    onOpenPicker: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DETECTED HEROES (${allEnemies.size}/5)",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Row {
            if (allEnemies.isNotEmpty()) {
                Text(
                    text = "Clear",
                    color = AccentPink,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { onClearAll() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "+ Manual",
                color = AccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onOpenPicker() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allEnemies, key = { it.id }) { hero ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp))
            ) {
                if (hero.portraitUrl.isNotEmpty()) {
                    AsyncImage(
                        model = hero.portraitUrl,
                        contentDescription = hero.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hero.name.take(2).uppercase(),
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Remove X button on top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(bottomStart = 6.dp))
                        .clickable { onRemoveEnemy(hero.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = TextWhite,
                        modifier = Modifier.size(10.dp)
                    )
                }

                // Name label at bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xAA000000))
                        .padding(vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hero.name,
                        color = TextWhite,
                        fontSize = 7.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Add placeholder slots up to 5
        val emptySlots = 5 - allEnemies.size
        items(emptySlots) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, BorderMuted, RoundedCornerShape(8.dp))
                    .clickable { onOpenPicker() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add hero",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Manual Hero Picker Dropdown / Sheet with Search and Role filtering
 */
@Composable
private fun ManualHeroPicker(
    availableHeroes: List<HeroEntity>,
    currentEnemyIds: Set<Int>,
    onHeroSelected: (HeroEntity) -> Unit,
    onDismiss: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterRole by remember { mutableStateOf<String?>(null) }

    val filteredHeroes = remember(searchQuery, filterRole, availableHeroes, currentEnemyIds) {
        availableHeroes.filter { hero ->
            !currentEnemyIds.contains(hero.id) &&
                (filterRole == null || filterRole == "All" || hero.role == filterRole || hero.secondaryRole == filterRole) &&
                (searchQuery.isBlank() || hero.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Enemy Hero",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search text field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBgBase)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextWhite, fontSize = 12.sp),
                    cursorBrush = SolidColor(AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(text = "Search heroes...", color = TextMuted, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Role selector inside picker
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ROLES_LIST) { role ->
                    val isSelected = (filterRole == role) || (filterRole == null && role == "All")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentCyan else DarkBgBase)
                            .clickable { filterRole = if (role == "All") null else role }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = role,
                            color = if (isSelected) DarkBgBase else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of matching heroes
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
            ) {
                items(filteredHeroes, key = { it.id }) { hero ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHeroSelected(hero) }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(DarkBgBase),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hero.name.take(1),
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hero.name,
                            color = TextWhite,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = hero.role,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Role Filter Pill Bar (All, Tank, Fighter, Assassin, Mage, Marksman, Support)
 */
@Composable
private fun RoleFilterBar(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ROLES_LIST.forEach { role ->
            val isSelected = selectedRole == role
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) AccentCyan else DarkSurface)
                    .border(
                        1.dp,
                        if (isSelected) AccentCyan else BorderMuted,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onRoleSelected(role) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = role,
                    color = if (isSelected) DarkBgBase else TextWhite,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Card displaying counter hero recommendations for one enemy hero
 */
@Composable
private fun EnemyCounterCard(
    enemy: HeroEntity,
    counters: List<Pair<HeroEntity, Int>>,
    masteredHeroIds: Set<Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Enemy Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "vs ${enemy.name}",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = enemy.role,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (counters.isEmpty()) {
                Text(
                    text = "No counters found for selected role filter.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                counters.take(3).forEach { (counterHero, score) ->
                    val isMastered = masteredHeroIds.contains(counterHero.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMastered) DarkSurfaceElevated else Color.Transparent)
                            .then(
                                if (isMastered) Modifier.border(1.dp, MasterGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Portrait / Avatar
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DarkBgBase)
                                .border(1.dp, if (isMastered) MasterGold else BorderMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (counterHero.portraitUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = counterHero.portraitUrl,
                                    contentDescription = counterHero.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            } else {
                                Text(
                                    text = counterHero.name.take(2).uppercase(),
                                    color = if (isMastered) MasterGold else TextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Name & Role
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = counterHero.name,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isMastered) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Mastered",
                                        tint = MasterGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                            Text(
                                text = counterHero.role,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }

                        // Counter Score Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$score%",
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying recommended counter items against the detected team
 */
@Composable
private fun CounterItemCard(
    item: ItemEntity,
    reason: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Item Icon Box
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, BorderMuted, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.iconUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Text(
                        text = item.name.take(2).uppercase(),
                        color = AccentPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.type,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = reason,
                    color = AccentCyan,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyEnemiesPrompt(onScanClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No enemy heroes detected yet.",
            color = TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap scan button during draft pick or add heroes manually with '+ Manual'.",
            color = TextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onScanClicked,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
            border = BorderStroke(1.dp, AccentCyan),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Scan Draft Screen", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
