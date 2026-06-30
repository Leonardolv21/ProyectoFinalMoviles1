package com.example.proyectofinalmoviles1.ui.screens.groups

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.proyectofinalmoviles1.data.api.GroupDetailResponse
import com.example.proyectofinalmoviles1.data.api.LeaderboardEntry
import kotlinx.coroutines.launch

class GroupDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val groupRepo = ServiceLocator.groupRepository

    var groupState by mutableStateOf<UiState<GroupDetailResponse>>(UiState.Idle)
        private set
    var leaderboardState by mutableStateOf<UiState<List<LeaderboardEntry>>>(UiState.Idle)
        private set

    fun load(groupId: Int) {
        groupState = UiState.Loading
        leaderboardState = UiState.Loading
        viewModelScope.launch {
            groupRepo.getGroupDetail(groupId).fold(
                onSuccess = { groupState = UiState.Success(it) },
                onFailure = { groupState = UiState.Error(it.message ?: "Error") }
            )
            groupRepo.getLeaderboard(groupId).fold(
                onSuccess = { leaderboardState = UiState.Success(it) },
                onFailure = { leaderboardState = UiState.Error(it.message ?: "Error") }
            )
        }
    }
}

@Composable
fun GroupDetailScreen(
    groupId: Int,
    onNavigateToMatch: (Int) -> Unit,
    viewModel: GroupDetailViewModel = viewModel()
) {
    LaunchedEffect(groupId) { viewModel.load(groupId) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        when (val state = viewModel.groupState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val group = state.data
                Text(group.name.ifEmpty { "Detalle del grupo" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (group.invite_code.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Código de invitación: ${group.invite_code}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))

                Text("Participantes: ${group.participants.size}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                group.participants.forEach { p ->
                    Text("  • ${p.name} — ${p.score} pts", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(16.dp))
                Text("Próximos partidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                group.next_matches.forEach { m ->
                    Card(onClick = { onNavigateToMatch(m.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${m.home_team} vs ${m.away_team}", style = MaterialTheme.typography.bodyLarge)
                                Text(m.match_date.take(10), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(m.phase, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            else -> {}
        }

        Spacer(Modifier.height(16.dp))
        Text("Clasificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        when (val lbState = viewModel.leaderboardState) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text(lbState.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                lbState.data.forEach { entry ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${entry.position}", fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                                Text(entry.name, modifier = Modifier.padding(start = 8.dp))
                            }
                            Text("${entry.score} pts", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
