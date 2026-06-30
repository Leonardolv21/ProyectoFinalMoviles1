package com.example.proyectofinalmoviles1.ui.screens.stadiums

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
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.api.StadiumResponse
import kotlinx.coroutines.launch

class StadiumDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val stadiumRepo = ServiceLocator.stadiumRepository

    var stadiumState by mutableStateOf<UiState<StadiumResponse>>(UiState.Idle)
        private set
    var matchesState by mutableStateOf<UiState<List<MatchResponse>>>(UiState.Idle)
        private set

    fun load(stadiumId: Int) {
        stadiumState = UiState.Loading
        matchesState = UiState.Loading
        viewModelScope.launch {
            stadiumRepo.getStadiumDetail(stadiumId).fold(
                onSuccess = { stadiumState = UiState.Success(it) },
                onFailure = { stadiumState = UiState.Error(it.message ?: "Error") }
            )
            stadiumRepo.getStadiumMatches(stadiumId).fold(
                onSuccess = { matchesState = UiState.Success(it) },
                onFailure = { matchesState = UiState.Error(it.message ?: "Error") }
            )
        }
    }
}

@Composable
fun StadiumDetailScreen(
    stadiumId: Int,
    onNavigateToMatch: (Int) -> Unit,
    viewModel: StadiumDetailViewModel = viewModel()
) {
    LaunchedEffect(stadiumId) { viewModel.load(stadiumId) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        when (val state = viewModel.stadiumState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val s = state.data
                Text(s.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("${s.city}, ${s.country}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Capacidad: ${s.capacity} espectadores", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("Coordenadas: ${s.latitude}, ${s.longitude}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {}
        }

        Spacer(Modifier.height(16.dp))
        Text("Partidos en este estadio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        when (val mState = viewModel.matchesState) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text(mState.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                if (mState.data.isEmpty()) {
                    Text("No hay partidos programados en este estadio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    mState.data.forEach { match ->
                        Card(onClick = { onNavigateToMatch(match.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${match.home_team} vs ${match.away_team}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(match.match_date.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall)
                                Text("${match.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
