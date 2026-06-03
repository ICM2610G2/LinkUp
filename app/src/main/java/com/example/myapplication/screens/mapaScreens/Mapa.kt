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
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.example.myapplication.repository.LocationRepository
import com.example.myapplication.repository.RaceRepository
import com.example.myapplication.sensors.LightSensorManager
import com.example.myapplication.sensors.NIGHT_MAP_STYLE
import com.example.myapplication.sensors.StepSensorManager
import com.google.android.gms.location.*
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

    // ── Pedir ubicación + actividad en un solo launcher ────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        viewModel.updateHasLocationPermission(locationGranted)
    }

    LaunchedEffect(Unit) {
        val locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val activityGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.updateHasLocationPermission(locationGranted)

        val toRequest = buildList {
            if (!locationGranted) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!activityGranted) add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        } else {
            viewModel.cargarDatos()
        }
    }

    if (state.hasLocationPermission) {
        MapaConUbicacion(viewModel = viewModel)
    } else {
        MapaSinPermiso(
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                )
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

    // ── Sensor de luz ──────────────────────────────────────────────
    val lightSensorManager = remember { LightSensorManager(context) }
    val isDark by lightSensorManager.isDark.collectAsState()

    DisposableEffect(Unit) {
        lightSensorManager.start()
        onDispose { lightSensorManager.stop() }
    }

    // ── Sensor de pasos ────────────────────────────────────────────
    val stepSensorManager = remember { StepSensorManager(context) }
    val steps by stepSensorManager.steps.collectAsState()
    val isMoving by stepSensorManager.isMoving.collectAsState()

    DisposableEffect(Unit) {
        stepSensorManager.start()
        onDispose { stepSensorManager.stop() }
    }
    // ───────────────────────────────────────────────────────────────

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
                    viewModel.cargarDatos()
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
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(3000L)
                .build(),
            callback,
            Looper.getMainLooper()
        )
        onDispose {
            locationRepository.removeLocation()
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    DisposableEffect(state.acceptedFriends) {
        val listeners = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()
        state.acceptedFriends.forEach { friend ->
            val ref = realtimeDb.child("user_live").child(friend.uid).child("location")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val visible = snapshot.child("visible").getValue(Boolean::class.java) ?: false
                    val lat = snapshot.child("lat").getValue(Double::class.java)
                    val lng = snapshot.child("lng").getValue(Double::class.java)

                    val updatedList = if (visible && lat != null && lng != null) {
                        state.friendLocations.filterNot { it.user.uid == friend.uid } +
                                FriendMapLocation(friend, LatLng(lat, lng))
                    } else {
                        state.friendLocations.filterNot { it.user.uid == friend.uid }
                    }
                    viewModel.updateFriendLocations(updatedList)
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            listeners.add(ref to listener)
        }
        onDispose { listeners.forEach { (ref, l) -> ref.removeEventListener(l) } }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapStyleOptions = if (isDark) MapStyleOptions(NIGHT_MAP_STYLE) else null
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            )
        ) {
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

            state.friendLocations.forEach { friendLocation ->
                Marker(
                    state = MarkerState(position = friendLocation.location),
                    title = friendLocation.user.displayName,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // ── Panel superior: estado de carrera ──────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            val session = state.activeSession
            val cardColor = when {
                session?.status == "active" -> Color(0xEEFF9800)
                session?.status == "lobby"  -> Color(0xEE2A9D8F)
                else                        -> Color(0xEE333333)
            }
            val textColor = if (session != null) Color.Black else Color.White

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (session != null) Icons.Default.DirectionsRun else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            session?.status == "active" -> "Carrera: ${session.raceName}"
                            session?.status == "lobby"  -> "Lobby: ${session.raceName} (Esperando inicio...)"
                            else -> "No estás inscrito a ninguna carrera. ¡Inscríbete a una!"
                        },
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── Widget podómetro (esquina inferior izquierda) ──────────
        StepWidget(
            steps = steps,
            isMoving = isMoving,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 120.dp)
        )

        // ── Panel inferior: validación de checkpoint ───────────────
        AnimatedVisibility(
            visible = selectedCheckpoint != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedCheckpoint?.let { cp ->
                val userLoc = state.userLocation
                val results = FloatArray(1)
                if (userLoc != null) {
                    Location.distanceBetween(
                        userLoc.latitude, userLoc.longitude,
                        cp.coordinates.latitude, cp.coordinates.longitude,
                        results
                    )
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
                            Text(
                                cp.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
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
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.PhotoCamera, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isWithinRange) "Tomar Foto del Checkpoint"
                                        else "Acércate a 50m para validar"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Widget podómetro ───────────────────────────────────────────────
@Composable
fun StepWidget(
    steps: Int,
    isMoving: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isMoving) Icons.Default.DirectionsWalk else Icons.Default.Accessibility,
                contentDescription = null,
                tint = if (isMoving) Color(0xFFFF9800) else Color(0xFF757575),
                modifier = Modifier
                    .size(20.dp)
                    .scale(if (isMoving) pulse else 1f)
            )
            Column {
                Text(
                    text = "$steps",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp
                )
                Text(
                    text = if (isMoving) "en marcha" else "pasos",
                    color = if (isMoving) Color(0xFFFF9800) else Color(0xFF757575),
                    fontSize = 9.sp,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
fun MapaSinPermiso(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ubicación requerida",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Para participar en carreras y ver amigos, activa los permisos.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dar permiso de ubicación")
                }
            }
        }
    }
}