package com.example.myapplication.screens.mapaScreens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.model.FriendMapLocation
import com.example.myapplication.model.MapaViewModel
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.LocationRepository
import com.example.myapplication.repository.RaceRepository
import com.example.myapplication.repository.UserRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun Mapa(
    viewModel: MapaViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.mapaState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updateHasLocationPermission(granted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateHasLocationPermission(granted)

        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.cargarCarreraActiva()
        }
    }

    if (state.hasLocationPermission) {
        MapaConUbicacion(viewModel = viewModel)
    } else {
        MapaSinPermiso(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapaConUbicacion(
    viewModel: MapaViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.mapaState.collectAsState()
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }

    var selectedCheckpoint by remember { mutableStateOf<Checkpoint?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedCheckpoint != null && state.activeSession != null) {
            scope.launch {
                isUploading = true
                val result = raceRepository.uploadCheckpointPhoto(
                    state.activeSession!!.id,
                    selectedCheckpoint!!.id,
                    uri
                )
                result.onSuccess {
                    Toast.makeText(context, "¡Checkpoint validado con éxito!", Toast.LENGTH_SHORT).show()
                    viewModel.cargarCarreraActiva()
                    selectedCheckpoint = null
                }.onFailure {
                    Toast.makeText(context, "Error al subir: ${it.message}", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    val locationRepository = remember { LocationRepository() }
    val realtimeDb = remember { FirebaseDatabase.getInstance().reference }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6097, -74.0817), 13f)
    }

    DisposableEffect(Unit) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)
                viewModel.updateUserLocation(latLng)

                val shouldShare = state.shareLocationMode == "always" || state.shareLocationMode == "in_race"
                locationRepository.updateUserLocation(location.latitude, location.longitude, shouldShare)
            }
        }
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).setMinUpdateIntervalMillis(3000L).build(),
            callback, Looper.getMainLooper()
        )
        onDispose {
            locationRepository.removeLocation()
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
        ) {
            // Checkpoints
            state.checkpoints.forEach { checkpoint ->
                val pos = LatLng(checkpoint.coordinates.latitude, checkpoint.coordinates.longitude)
                val isCompleted = state.activeSession?.participants?.get(auth.currentUser?.uid)
                    ?.checkpointsDone?.contains(checkpoint.id) == true

                Marker(
                    state = MarkerState(position = pos),
                    title = checkpoint.name,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (isCompleted) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_AZURE
                    ),
                    onClick = {
                        selectedCheckpoint = checkpoint
                        false
                    }
                )

                Circle(
                    center = pos,
                    radius = 50.0,
                    fillColor = if (isCompleted) Color(0x334CAF50) else Color(0x332196F3),
                    strokeColor = if (isCompleted) Color.Green else Color.Blue,
                    strokeWidth = 2f
                )
            }

            // Amigos
            state.friendLocations.forEach { friendLocation ->
                Marker(
                    state = MarkerState(position = friendLocation.location),
                    title = friendLocation.user.displayName,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // --- PANEL SUPERIOR: Nombre de Carrera o Aviso ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.activeSession != null) Color(0xEEFF9800) else Color(0xEE333333)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.activeSession != null) Icons.Default.DirectionsRun else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (state.activeSession != null) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.activeSession?.let { "Carrera: ${it.raceName}" }
                            ?: "No estás inscrito a ninguna carrera",
                        color = if (state.activeSession != null) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // --- PANEL INFERIOR: Validación de Checkpoint ---
        AnimatedVisibility(
            visible = selectedCheckpoint != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedCheckpoint?.let { cp ->
                val userLoc = state.userLocation
                val results = FloatArray(1)
                if (userLoc != null) {
                    Location.distanceBetween(userLoc.latitude, userLoc.longitude, cp.coordinates.latitude, cp.coordinates.longitude, results)
                }
                val distance = if (userLoc != null) results[0] else Float.MAX_VALUE
                val isWithinRange = distance <= 50f
                val isCompleted = state.activeSession?.participants?.get(auth.currentUser?.uid)
                    ?.checkpointsDone?.contains(cp.id) == true

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { selectedCheckpoint = null }) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray)
                            }
                        }

                        Text(
                            text = if (isCompleted) "¡Punto completado!" else "Distancia: ${distance.toInt()}m",
                            color = if (isWithinRange || isCompleted) Color.Green else Color.Red,
                            fontSize = 14.sp
                        )

                        if (!isCompleted) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { photoLauncher.launch("image/*") },
                                enabled = isWithinRange && !isUploading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PhotoCamera, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isWithinRange) "Tomar Foto del Checkpoint" else "Acércate a 50m para validar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapaSinPermiso(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0B0B0B)), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOff, null, tint = Color(0xFFFF9800), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ubicación requerida", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Para participar en carreras y ver amigos, activa los permisos.", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRequestPermission, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), shape = RoundedCornerShape(12.dp)) {
                    Text("Dar permiso de ubicación")
                }
            }
        }
    }
}