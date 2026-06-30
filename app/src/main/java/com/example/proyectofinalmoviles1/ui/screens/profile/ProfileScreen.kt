package com.example.proyectofinalmoviles1.ui.screens.profile

import android.app.Application
import androidx.compose.foundation.layout.*
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
import com.example.proyectofinalmoviles1.data.TokenProvider
import com.example.proyectofinalmoviles1.data.UiState
import com.example.proyectofinalmoviles1.data.api.ProfileResponse
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val profileRepo = ServiceLocator.profileRepository
    private val authRepo = ServiceLocator.authRepository

    var profileState by mutableStateOf<UiState<ProfileResponse>>(UiState.Idle)
        private set

    init { load() }

    fun load() {
        profileState = UiState.Loading
        viewModelScope.launch {
            profileRepo.getProfile().fold(
                onSuccess = { profileState = UiState.Success(it) },
                onFailure = { profileState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout(TokenProvider.token ?: "")
            TokenProvider.token = null
            onDone()
        }
    }
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        when (val state = viewModel.profileState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val p = state.data
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(p.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatCard("Puntaje", "${p.total_score}")
                            StatCard("Grupos", "${p.groups_count}")
                            StatCard("Pronósticos", "${p.predictions_count}")
                        }
                    }
                }
            }
            else -> {}
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = { viewModel.logout(onLogout) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar sesión")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.width(100.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
