package com.example.proyectofinalmoviles1.ui.screens.stadiums

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinalmoviles1.ServiceLocator
import com.example.proyectofinalmoviles1.data.UiState
import com.example.proyectofinalmoviles1.data.api.StadiumResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val defaultCenter = LatLng(39.8283, -98.5795)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 3.5f)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation = latLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 7f)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sedes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Estadios del Mundial 2026", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        when (val state = viewModel.stadiumsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                LaunchedEffect(state.data) {
                    if (userLocation == null && state.data.isNotEmpty()) {
                        val first = state.data.first()
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(first.latitude, first.longitude),
                            4f
                        )
                    }
                }

                Card(Modifier.fillMaxWidth().height(320.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = hasLocationPermission
                        )
                    ) {
                        state.data.forEach { stadium ->
                            Marker(
                                state = MarkerState(LatLng(stadium.latitude, stadium.longitude)),
                                title = stadium.name,
                                snippet = "${stadium.city}, ${stadium.country}",
                                onClick = {
                                    onNavigateToStadium(stadium.id)
                                    true
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                if (!hasLocationPermission) {
                    FilledTonalButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar mi ubicación")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Text("Lista de estadios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                LazyColumn(Modifier.weight(1f)) {
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

private fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
