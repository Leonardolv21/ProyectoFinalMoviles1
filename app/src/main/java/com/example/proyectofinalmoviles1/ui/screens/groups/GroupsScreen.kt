package com.example.proyectofinalmoviles1.ui.screens.groups

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
import com.example.proyectofinalmoviles1.data.api.CreateGroupResponse
import com.example.proyectofinalmoviles1.data.api.GroupResponse
import com.example.proyectofinalmoviles1.data.api.JoinGroupResponse
import kotlinx.coroutines.launch

class GroupsViewModel(application: Application) : AndroidViewModel(application) {
    private val groupRepo = ServiceLocator.groupRepository

    var groupsState by mutableStateOf<UiState<List<GroupResponse>>>(UiState.Idle)
        private set
    var createResult by mutableStateOf<UiState<CreateGroupResponse>>(UiState.Idle)
        private set
    var joinResult by mutableStateOf<UiState<JoinGroupResponse>>(UiState.Idle)
        private set
    var showCreateDialog by mutableStateOf(false)
    var showJoinDialog by mutableStateOf(false)

    init { loadGroups() }

    fun loadGroups() {
        groupsState = UiState.Loading
        viewModelScope.launch {
            groupRepo.getGroups().fold(
                onSuccess = { groupsState = UiState.Success(it) },
                onFailure = { groupsState = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun createGroup(name: String) {
        createResult = UiState.Loading
        viewModelScope.launch {
            groupRepo.createGroup(name).fold(
                onSuccess = {
                    createResult = UiState.Success(it)
                    loadGroups()
                    showCreateDialog = false
                },
                onFailure = { createResult = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun joinGroup(code: String) {
        joinResult = UiState.Loading
        viewModelScope.launch {
            groupRepo.joinGroup(code).fold(
                onSuccess = {
                    joinResult = UiState.Success(it)
                    loadGroups()
                    showJoinDialog = false
                },
                onFailure = { joinResult = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun resetResults() {
        createResult = UiState.Idle
        joinResult = UiState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onNavigateToGroup: (Int) -> Unit,
    viewModel: GroupsViewModel = viewModel()
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mis grupos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row {
                FilledTonalButton({ viewModel.showCreateDialog = true }, Modifier.padding(end = 4.dp)) { Text("Crear") }
                FilledTonalButton({ viewModel.showJoinDialog = true }) { Text("Unirse") }
            }
        }
        Spacer(Modifier.height(8.dp))

        when (val state = viewModel.groupsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes grupos. Crea uno o únete a un código.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn {
                        items(state.data) { group ->
                            Card(onClick = { onNavigateToGroup(group.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) {
                                        Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${group.participants_count} participantes", style = MaterialTheme.typography.bodySmall)
                                        Text("Código: ${group.invite_code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${group.user_score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("pts", style = MaterialTheme.typography.bodySmall)
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

    if (viewModel.showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showCreateDialog = false },
            title = { Text("Crear grupo") },
            text = {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre del grupo") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button({ viewModel.createGroup(name) }, enabled = name.isNotBlank()) { Text("Crear") }
            },
            dismissButton = { TextButton({ viewModel.showCreateDialog = false }) { Text("Cancelar") } }
        )
    }

    if (viewModel.showJoinDialog) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showJoinDialog = false },
            title = { Text("Unirse a un grupo") },
            text = {
                OutlinedTextField(code, { code = it }, label = { Text("Código de invitación") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button({ viewModel.joinGroup(code) }, enabled = code.isNotBlank()) { Text("Unirse") }
            },
            dismissButton = { TextButton({ viewModel.showJoinDialog = false }) { Text("Cancelar") } }
        )
    }
}
