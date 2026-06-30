package com.example.proyectofinalmoviles1.ui.screens.stadiums

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
import com.example.proyectofinalmoviles1.data.api.StadiumResponse
import kotlinx.coroutines.launch

class StadiumMapViewModel(application: Application) : AndroidViewModel(application) {
    private val stadiumRepo = ServiceLocator.stadiumRepository

    var stadiumsState by mutableStateOf<UiState<List<StadiumResponse>>>(UiState.Idle)
        private set

    init { load() }

    fun load() {
        stadiumsState = UiState.Loading
        viewModelScope.launch {
            stadiumRepo.getStadiums().fold(
                onSuccess = { stadiumsState = UiState.Success(it) },
                onFailure = { stadiumsState = UiState.Error(it.message ?: "Error") }
            )
        }
    }
}

@Composable
fun StadiumMapScreen(
    onNavigateToStadium: (Int) -> Unit,
    viewModel: StadiumMapViewModel = viewModel()
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sedes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Estadios del Mundial 2026", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Mapa interactivo requiere configuración de Google Maps API key.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Agrega tu API key en AndroidManifest.xml:", style = MaterialTheme.typography.bodySmall)
                Text(
                    "<meta-data android:name=\"com.google.android.geo.API_KEY\"\n    android:value=\"TU_API_KEY\"/>",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Lista de estadios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        when (val state = viewModel.stadiumsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                LazyColumn {
                    items(state.data) { stadium ->
                        Card(onClick = { onNavigateToStadium(stadium.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(stadium.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${stadium.city}, ${stadium.country}", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${stadium.capacity}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("capacidad", style = MaterialTheme.typography.bodySmall)
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
