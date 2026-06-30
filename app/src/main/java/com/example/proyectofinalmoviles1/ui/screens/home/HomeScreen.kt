package com.example.proyectofinalmoviles1.ui.screens.home

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
import com.example.proyectofinalmoviles1.data.api.GroupResponse
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.api.ProfileResponse
import kotlinx.coroutines.launch

data class HomeData(
    val profile: ProfileResponse? = null,
    val groups: List<GroupResponse> = emptyList(),
    val nextMatches: List<MatchResponse> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val profileRepo = ServiceLocator.profileRepository
    private val groupRepo = ServiceLocator.groupRepository
    private val matchRepo = ServiceLocator.matchRepository

    var uiState by mutableStateOf<UiState<HomeData>>(UiState.Idle)
        private set

    init { loadData() }

    fun loadData() {
        uiState = UiState.Loading
        viewModelScope.launch {
            val profileResult = profileRepo.getProfile()
            val groupsResult = groupRepo.getGroups()
            val matchesResult = matchRepo.getMatches(next = true)

            val profile = profileResult.getOrNull()
            val groups = groupsResult.getOrNull() ?: emptyList()
            val matches = matchesResult.getOrNull() ?: emptyList()

            uiState = UiState.Success(HomeData(profile, groups, matches))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGroups: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToGroup: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Inicio", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        when (val state = viewModel.uiState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val data = state.data

                data.profile?.let { profile ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(profile.name, style = MaterialTheme.typography.titleLarge)
                            Text(profile.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("Puntaje", "${profile.total_score}")
                                StatItem("Grupos", "${profile.groups_count}")
                                StatItem("Pronósticos", "${profile.predictions_count}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mis grupos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onNavigateToGroups) { Text("Ver todos") }
                        }
                        if (data.groups.isEmpty()) {
                            Text("Sin grupos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            data.groups.take(3).forEach { group ->
                                GroupCard(group, onClick = { onNavigateToGroup(group.id) })
                                if (group != data.groups.lastOrNull()) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Próximos partidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onNavigateToMatches) { Text("Ver todos") }
                        }
                        if (data.nextMatches.isEmpty()) {
                            Text("Sin partidos próximos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            data.nextMatches.take(5).forEach { match ->
                                MatchCard(match, onClick = { onNavigateToMatch(match.id) })
                                if (match != data.nextMatches.lastOrNull()) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GroupCard(group: GroupResponse, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(group.name, style = MaterialTheme.typography.bodyLarge)
                Text("${group.participants_count} participantes · Código: ${group.invite_code}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${group.user_score}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("pts", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MatchCard(match: MatchResponse, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("${match.home_team} vs ${match.away_team}", style = MaterialTheme.typography.bodyLarge)
                Text(match.match_date.take(10), style = MaterialTheme.typography.bodySmall)
            }
            val statusText = when (match.status) {
                "scheduled" -> "Programado"
                "live" -> "En vivo"
                "finished" -> "Finalizado"
                else -> match.status
            }
            Text(statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
