package com.example.proyectofinalmoviles1.ui.screens.matches

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinalmoviles1.ServiceLocator
import com.example.proyectofinalmoviles1.data.UiState
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.api.PredictionResponse
import kotlinx.coroutines.launch

class MatchDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val matchRepo = ServiceLocator.matchRepository
    private val predictionRepo = ServiceLocator.predictionRepository

    var matchState by mutableStateOf<UiState<MatchResponse>>(UiState.Idle)
        private set
    var predictionState by mutableStateOf<UiState<PredictionResponse>>(UiState.Idle)
        private set
    var homeScore by mutableStateOf("")
    var awayScore by mutableStateOf("")
    var predictionMessage by mutableStateOf<String?>(null)

    fun load(matchId: Int) {
        matchState = UiState.Loading
        viewModelScope.launch {
            matchRepo.getMatchDetail(matchId).fold(
                onSuccess = { matchState = UiState.Success(it) },
                onFailure = { matchState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun submitPrediction(matchId: Int) {
        val h = homeScore.toIntOrNull() ?: return
        val a = awayScore.toIntOrNull() ?: return
        predictionState = UiState.Loading
        predictionMessage = null
        viewModelScope.launch {
            predictionRepo.createPrediction(matchId, h, a).fold(
                onSuccess = {
                    predictionState = UiState.Success(it)
                    predictionMessage = it.message
                },
                onFailure = {
                    predictionState = UiState.Error(it.message ?: "Error")
                    predictionMessage = it.message
                }
            )
        }
    }
}

@Composable
fun MatchDetailScreen(
    matchId: Int,
    onNavigateToStadium: (Int) -> Unit,
    viewModel: MatchDetailViewModel = viewModel()
) {
    LaunchedEffect(matchId) { viewModel.load(matchId) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        when (val state = viewModel.matchState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val match = state.data
                Text("${match.home_team} vs ${match.away_team}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Fecha: ${match.match_date.take(16).replace("T", " ")}", style = MaterialTheme.typography.bodyLarge)
                Text("Fase: ${match.phase.replace("_", " ")}", style = MaterialTheme.typography.bodyLarge)
                Text("Estado: ${match.status}", style = MaterialTheme.typography.bodyLarge)
                match.group_name?.let { Text("Grupo: $it", style = MaterialTheme.typography.bodyLarge) }
                match.stadium?.let { Text("Estadio: $it", style = MaterialTheme.typography.bodyLarge) }
                Spacer(Modifier.height(8.dp))
                val scoreText = if (match.home_score != null && match.away_score != null) "${match.home_score} - ${match.away_score}" else "—"
                Text("Resultado: $scoreText", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text("Tu pronóstico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(viewModel.homeScore, { viewModel.homeScore = it }, label = { Text("Goles local") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(viewModel.awayScore, { viewModel.awayScore = it }, label = { Text("Goles visita") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    { viewModel.submitPrediction(matchId) },
                    enabled = match.status == "scheduled" && viewModel.homeScore.isNotBlank() && viewModel.awayScore.isNotBlank()
                ) { Text("Registrar pronóstico") }

                viewModel.predictionMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = if (it.contains("Error", ignoreCase = true) || it.contains("No se puede", ignoreCase = true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }
            else -> {}
        }
    }
}
