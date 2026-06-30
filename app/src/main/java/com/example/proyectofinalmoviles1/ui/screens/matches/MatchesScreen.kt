package com.example.proyectofinalmoviles1.ui.screens.matches

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinalmoviles1.ServiceLocator
import com.example.proyectofinalmoviles1.data.UiState
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import kotlinx.coroutines.launch

class MatchesViewModel(application: Application) : AndroidViewModel(application) {
    private val matchRepo = ServiceLocator.matchRepository

    var matchesState by mutableStateOf<UiState<List<MatchResponse>>>(UiState.Idle)
        private set
    var selectedPhase by mutableStateOf("")
    var selectedStatus by mutableStateOf("")
    var selectedDate by mutableStateOf("")

    init { loadAll() }

    fun loadAll() {
        selectedPhase = ""; selectedStatus = ""; selectedDate = ""
        matchesState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatches().fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun loadNext() {
        selectedPhase = ""; selectedStatus = ""; selectedDate = ""
        matchesState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatches(next = true).fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun filterByPhase(phase: String) {
        selectedPhase = phase
        matchesState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatches(phase = phase.ifBlank { null }).fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun filterByStatus(status: String) {
        selectedStatus = status
        matchesState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatches(status = status.ifBlank { null }).fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun filterByDate(date: String) {
        selectedDate = date
        matchesState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatches(date = date.ifBlank { null }).fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    onNavigateToMatch: (Int) -> Unit,
    viewModel: MatchesViewModel = viewModel()
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Partidos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Todos", viewModel.selectedPhase.isEmpty() && viewModel.selectedStatus.isEmpty() && viewModel.selectedDate.isEmpty()) { viewModel.loadAll() }
            FilterChip("Próximos", viewModel.selectedPhase.isEmpty() && viewModel.selectedStatus.isEmpty() && viewModel.selectedDate.isEmpty() && false) { viewModel.loadNext() }
        }
        Spacer(Modifier.height(8.dp))

        var phaseExpanded by remember { mutableStateOf(false) }
        val phases = listOf("group", "round_of_32", "round_of_16", "quarter", "semi", "third_place", "final")
        ExposedDropdownMenuBox(phaseExpanded, { phaseExpanded = !phaseExpanded }) {
            OutlinedTextField(
                value = viewModel.selectedPhase.ifEmpty { "Filtrar por fase" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(phaseExpanded) }
            )
            ExposedDropdownMenu(phaseExpanded, { phaseExpanded = false }) {
                phases.forEach { p ->
                    DropdownMenuItem(text = { Text(p.replace("_", " ")) }, onClick = { viewModel.filterByPhase(p); phaseExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        var statusExpanded by remember { mutableStateOf(false) }
        val statuses = listOf("scheduled", "live", "finished")
        ExposedDropdownMenuBox(statusExpanded, { statusExpanded = !statusExpanded }) {
            OutlinedTextField(
                value = viewModel.selectedStatus.ifEmpty { "Filtrar por estado" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) }
            )
            ExposedDropdownMenu(statusExpanded, { statusExpanded = false }) {
                statuses.forEach { s ->
                    DropdownMenuItem(text = { Text(s) }, onClick = { viewModel.filterByStatus(s); statusExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        var dateInput by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(dateInput, { dateInput = it }, label = { Text("YYYY-MM-DD") }, modifier = Modifier.weight(1f))
            Button({ if (dateInput.isNotBlank()) viewModel.filterByDate(dateInput) }) { Text("Buscar") }
        }

        Spacer(Modifier.height(8.dp))

        when (val state = viewModel.matchesState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron partidos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn {
                        items(state.data) { match ->
                            Card(onClick = { onNavigateToMatch(match.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${match.home_team} vs ${match.away_team}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(match.match_date.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall)
                                        Text(match.phase.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row {
                                        val scoreText = if (match.home_score != null && match.away_score != null) "${match.home_score} - ${match.away_score}" else "vs"
                                        Text(scoreText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(8.dp))
                                        Text(match.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(onClick = onClick, label = { Text(text) }, selected = selected)
}
