package com.alexfandos.kyudotempo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KyudoScreen(viewModel: KyudoViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kyudo Tachi Tempo Guide") },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.previousStep() },
                        enabled = uiState.currentStepIndex > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Text(
                        text = "Step ${uiState.currentStepIndex + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.nextStep() },
                        enabled = !uiState.isCompleted
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val phaseContainerColor = when (uiState.phase) {
                TachiPhase.FIRST_ARROW -> MaterialTheme.colorScheme.primaryContainer
                TachiPhase.SECOND_ARROW -> MaterialTheme.colorScheme.tertiaryContainer
                TachiPhase.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
            }

            val phaseContentColor = when (uiState.phase) {
                TachiPhase.FIRST_ARROW -> MaterialTheme.colorScheme.onPrimaryContainer
                TachiPhase.SECOND_ARROW -> MaterialTheme.colorScheme.onTertiaryContainer
                TachiPhase.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            // Phase Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = phaseContainerColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "CURRENT PHASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = phaseContentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.phase.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = phaseContentColor
                    )
                }
            }

            // Tachi Size Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tachi Size",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 4, 5).forEach { size ->
                        FilterChip(
                            selected = uiState.tachiSize == size,
                            onClick = { viewModel.setTachiSize(size) },
                            label = {
                                Text(
                                    text = "$size Archers",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                text = "Tachi Lineup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Archers List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.archers) { archer ->
                    ArcherRow(archer = archer)
                }
            }
        }
    }
}

@Composable
fun ArcherRow(archer: Archer) {
    val containerColor = when (archer.state) {
        ArcherState.PREPARING_ARROWS,
        ArcherState.PREPARING_SECOND_ARROW -> MaterialTheme.colorScheme.secondaryContainer
        ArcherState.SHOOTING -> MaterialTheme.colorScheme.tertiaryContainer
        ArcherState.STANDING_REMOVING_ARROW -> MaterialTheme.colorScheme.errorContainer
        ArcherState.STANDBY -> MaterialTheme.colorScheme.primaryContainer
        ArcherState.SITTING_DOWN -> MaterialTheme.colorScheme.surfaceVariant
        ArcherState.GONE -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (archer.state) {
        ArcherState.PREPARING_ARROWS,
        ArcherState.PREPARING_SECOND_ARROW -> MaterialTheme.colorScheme.onSecondaryContainer
        ArcherState.SHOOTING -> MaterialTheme.colorScheme.onTertiaryContainer
        ArcherState.STANDING_REMOVING_ARROW -> MaterialTheme.colorScheme.onErrorContainer
        ArcherState.STANDBY -> MaterialTheme.colorScheme.onPrimaryContainer
        ArcherState.SITTING_DOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        ArcherState.GONE -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${archer.id}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = archer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = archer.state.actionHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Arrow Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arrows: ${archer.arrowCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
