package com.example.proyectofinalmoviles1.ui.screens.login

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.proyectofinalmoviles1.ServiceLocator
import com.example.proyectofinalmoviles1.data.TokenProvider
import com.example.proyectofinalmoviles1.data.UiState

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = ServiceLocator.authRepository

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegister by mutableStateOf(false)
    var name by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var uiState by mutableStateOf<UiState<Unit>>(UiState.Idle)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        checkSession()
    }

    private fun checkSession() {
        if (TokenProvider.token != null) {
            uiState = UiState.Success(Unit)
        }
    }

    fun submit() {
        if (isRegister) {
            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorMessage = "Todos los campos son obligatorios"
                return
            }
            if (password != confirmPassword) {
                errorMessage = "Las contraseñas no coinciden"
                return
            }
            if (password.length < 8) {
                errorMessage = "La contraseña debe tener al menos 8 caracteres"
                return
            }
        } else {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Correo y contraseña obligatorios"
                return
            }
        }
        errorMessage = null
        uiState = UiState.Loading

        viewModelScope.launch {
            val result = if (isRegister) {
                authRepo.register(name, email, password, confirmPassword)
            } else {
                authRepo.login(email, password)
            }
            result.fold(
                onSuccess = {
                    TokenProvider.token = it.token
                    uiState = UiState.Success(Unit)
                },
                onFailure = {
                    uiState = UiState.Idle
                    errorMessage = it.message ?: "Error desconocido"
                }
            )
        }
    }

    fun clearError() { errorMessage = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text("Quiniela 2026", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Mundial FIFA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))

        if (viewModel.isRegister) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it; viewModel.clearError() },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it; viewModel.clearError() },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it; viewModel.clearError() },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (viewModel.isRegister) ImeAction.Next else ImeAction.Done)
        )
        Spacer(Modifier.height(12.dp))

        if (viewModel.isRegister) {
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it; viewModel.clearError() },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(12.dp))
        }

        viewModel.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.submit() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = state !is UiState.Loading
        ) {
            if (state is UiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (viewModel.isRegister) "Registrarse" else "Iniciar sesión")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { viewModel.isRegister = !viewModel.isRegister; viewModel.clearError() }) {
            Text(
                if (viewModel.isRegister) "¿Ya tienes cuenta? Inicia sesión"
                else "¿No tienes cuenta? Regístrate"
            )
        }
    }
}
