package com.example.myapplication.screens.mapaScreens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
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
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.model.FriendMapLocation
import com.example.myapplication.model.MapaViewModel
import com.example.myapplication.model.ParticipantMapLocation
import com.example.myapplication.model.RutaMode
import com.example.myapplication.repository.FriendsRepository
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
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.repository.UserRepository
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

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
        if (granted) {
            viewModel.cargarCarreraActiva()
        }
    }

    // Recalcular ruta secuencial cuando cambia ubicación
    LaunchedEffect(state.userLocation, state.rutaMode) {
        if (state.rutaMode == RutaMode.SEQUENTIAL && state.userLocation != null && state.checkpoints.isNotEmpty()) {
            viewModel.calcularRutas(state.rutaMode)
        }
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
            viewModel.cargarDatos()
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
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val userRepository = remember { UserRepository() }
    val friendsRepository = remember { FriendsRepository() }
    val locationRepository = remember { LocationRepository() }
    val realtimeDb = remember {
        FirebaseDatabase.getInstance(
            "https://linkup-99296-default-rtdb.firebaseio.com/"
        ).reference
    }

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
    var compartirUbicacion by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(4.6097, -74.0817),
            13f
        )
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedCheckpoint != null && state.activeSession != null) {
            scope.launch {
                isUploading = true
                val result = raceRepository.uploadCheckpointPhoto(
                    sessionId = state.activeSession!!.id,
                    checkpointId = selectedCheckpoint!!.id,
                    imageUri = uri
                )
                result.onSuccess {
                    Toast.makeText(context, "¡Checkpoint validado con éxito!", Toast.LENGTH_SHORT).show()
                    viewModel.cargarDatos()
                    selectedCheckpoint = null
                }
                result.onFailure {
                    Toast.makeText(context, "Error al subir: ${it.message}", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }

    // ============================================================
    // CARGAR DATOS INICIALES
    // ============================================================
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val user = userRepository.getUser(uid)
        viewModel.updateShareLocationMode(user?.shareLocationMode ?: "in_race")
        val friends = friendsRepository.getAcceptedFriends()
        viewModel.updateAcceptedFriends(friends)
        viewModel.cargarCarreraActiva()
    }

    // ============================================================
    // ESCUCHAR CAMBIOS DE SESIÓN ACTIVA EN FIRESTORE
    // ============================================================
    DisposableEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("race_sessions")
                .whereArrayContains("participantIds", uid)
                .addSnapshotListener { snapshot, _ ->
                    val activeSession = snapshot?.documents
                        ?.mapNotNull { doc ->
                            doc.toObject(RaceSession::class.java)?.apply {
                                id = doc.id
                            }
                        }
                        ?.firstOrNull { it.status == "active" }

                    Log.d("MAP_SESSION", "Sesión activa detectada: ${activeSession?.id}")
                    viewModel.updateActiveSession(activeSession)

                    if (activeSession != null) {
                        scope.launch {
                            val checkpoints = raceRepository.getCheckpoints(activeSession.raceId)
                            viewModel.updateCheckpoints(checkpoints)
                        }
                    } else {
                        viewModel.updateCheckpoints(emptyList())
                        viewModel.updateParticipantLocations(emptyList())
                    }
                }
            onDispose { listener.remove() }
        }
    }

    // ============================================================
    // UBICACIÓN EN TIEMPO REAL
    // ============================================================
    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)
                val currentActiveSession = state.activeSession
                val activeSessionId = currentActiveSession?.id
                val uid = auth.currentUser?.uid ?: return

                viewModel.updateUserLocation(latLng)

                Log.d("MAP_LOCATION", "Ubicación: lat=${location.latitude}, lng=${location.longitude}")
                Log.d("MAP_LOCATION", "activeSessionId: $activeSessionId, compartirUbicacion: $compartirUbicacion")

                if (compartirUbicacion) {
                    if (activeSessionId != null) {
                        Log.d("MAP_LOCATION", "Escribiendo en live_positions/$activeSessionId/$uid")
                        locationRepository.updateRaceLocation(
                            sessionId = activeSessionId,
                            lat = location.latitude,
                            lng = location.longitude
                        )
                    } else {
                        Log.d("MAP_LOCATION", "Escribiendo en user_live/$uid")
                        locationRepository.updateUserLocation(
                            lat = location.latitude,
                            lng = location.longitude,
                            visible = true
                        )
                    }
                } else {
                    if (activeSessionId != null) {
                        locationRepository.removeRaceLocation(activeSessionId)
                    } else {
                        locationRepository.updateUserLocation(
                            lat = location.latitude,
                            lng = location.longitude,
                            visible = false
                        )
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                Log.d("MAP_LOCATION", "LocationAvailability: ${availability.isLocationAvailable}")
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(callback)
            state.activeSession?.id?.let { sessionId ->
                locationRepository.removeRaceLocation(sessionId)
            }
            locationRepository.removeLocation()
        }
    }

    // ============================================================
    // FORZAR ESCRITURA INICIAL
    // ============================================================
    LaunchedEffect(state.activeSession, state.userLocation) {
        val session = state.activeSession
        val userLocation = state.userLocation

        Log.d("MAP_SESSION_CHECK", "Verificando: session=$session, userLocation=$userLocation, compartir=$compartirUbicacion")

        if (session != null && userLocation != null && compartirUbicacion) {
            Log.d("MAP_SESSION_CHECK", "Forzando escritura inicial en live_positions")
            locationRepository.updateRaceLocation(
                sessionId = session.id,
                lat = userLocation.latitude,
                lng = userLocation.longitude
            )
        }
    }

    // ============================================================
    // ESCUCHAR PARTICIPANTES EN live_positions
    // ============================================================
    DisposableEffect(state.activeSession?.id) {
        val sessionId = state.activeSession?.id
        Log.d("MAP_PARTICIPANTS", "Escuchando participantes para sesión: $sessionId")

        if (sessionId == null) {
            viewModel.updateParticipantLocations(emptyList())
            onDispose { }
        } else {
            val ref = realtimeDb.child("live_positions").child(sessionId)
            Log.d("MAP_PARTICIPANTS", "Ruta: $ref")

            val participantsCache = mutableMapOf<String, Pair<String, String>>()

            scope.launch {
                val session = state.activeSession
                session?.participantIds?.forEach { uid ->
                    if (uid != auth.currentUser?.uid) {
                        val user = userRepository.getUser(uid)
                        if (user != null) {
                            participantsCache[uid] = Pair(user.displayName, user.gameId)
                            Log.d("MAP_PARTICIPANTS", "Cargado: $uid -> ${user.displayName}, ${user.gameId}")
                        }
                    }
                }
            }

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("MAP_PARTICIPANTS", "DataSnapshot recibido. Children: ${snapshot.childrenCount}")

                    val updatedList = snapshot.children.mapNotNull { child ->
                        val uid = child.key ?: return@mapNotNull null
                        val lat = child.child("lat").getValue(Double::class.java)
                        val lng = child.child("lng").getValue(Double::class.java)

                        if (lat != null && lng != null) {
                            val isCurrentUser = uid == auth.currentUser?.uid

                            val displayName = if (isCurrentUser) {
                                "Tú"
                            } else {
                                participantsCache[uid]?.first ?: "Participante"
                            }

                            val gameId = if (isCurrentUser) {
                                "Tú"
                            } else {
                                participantsCache[uid]?.second ?: ""
                            }

                            Log.d("MAP_PARTICIPANTS", "Participante: $uid, nombre=$displayName, gameId=$gameId")

                            ParticipantMapLocation(
                                uid = uid,
                                displayName = displayName,
                                gameId = gameId,
                                location = LatLng(lat, lng)
                            )
                        } else {
                            null
                        }
                    }

                    Log.d("MAP_PARTICIPANTS", "Participantes encontrados: ${updatedList.size}")
                    viewModel.updateParticipantLocations(updatedList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MAP_PARTICIPANTS", "Error: ${error.message}")
                }
            }

            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }
    }

    // ============================================================
    // ESCUCHAR AMIGOS (solo fuera de carrera)
    // ============================================================
    DisposableEffect(state.acceptedFriends, state.activeSession?.id) {
        if (state.activeSession != null) {
            viewModel.updateFriendLocations(emptyList())
            onDispose { }
        } else {
            val listeners = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()

            state.acceptedFriends.forEach { friend ->
                val ref = realtimeDb.child("user_live").child(friend.uid).child("location")

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val visible = snapshot.child("visible").getValue(Boolean::class.java) ?: false
                        val lat = snapshot.child("lat").getValue(Double::class.java)
                        val lng = snapshot.child("lng").getValue(Double::class.java)

                        val updatedList = if (visible && lat != null && lng != null) {
                            val newLocation = FriendMapLocation(
                                user = friend,
                                location = LatLng(lat, lng)
                            )
                            state.friendLocations.filterNot { it.user.uid == friend.uid } + newLocation
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

            onDispose {
                listeners.forEach { (ref, listener) ->
                    ref.removeEventListener(listener)
                }
            }
        }
    }

    // ============================================================
    // UI DEL MAPA
    // ============================================================
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
            // ── CHECKPOINTS ─────────────────────────────────────────
            state.checkpoints.forEach { checkpoint ->
                val pos = LatLng(checkpoint.coordinates.latitude, checkpoint.coordinates.longitude)
                val isCompleted = state.activeSession
                    ?.participants
                    ?.get(auth.currentUser?.uid)
                    ?.checkpointsDone
                    ?.contains(checkpoint.id) == true

                val isSelectedForRoute = state.selectedRouteCheckpoint?.id == checkpoint.id

                Marker(
                    state = MarkerState(position = pos),
                    title = checkpoint.name,
                    snippet = when {
                        isCompleted -> "✓ Completado"
                        isSelectedForRoute -> "📍 Ruta activa"
                        else -> "Pendiente"
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(
                        when {
                            isCompleted -> BitmapDescriptorFactory.HUE_GREEN
                            isSelectedForRoute -> BitmapDescriptorFactory.HUE_YELLOW
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }
                    ),
                    onClick = {
                        when (state.rutaMode) {
                            RutaMode.SINGLE -> {
                                // En modo SINGLE: calcular ruta a este checkpoint
                                viewModel.selectRouteCheckpoint(checkpoint)
                                false
                            }
                            else -> {
                                // Comportamiento original: abrir panel de validación
                                selectedCheckpoint = checkpoint
                                false
                            }
                        }
                    }
                )

                Circle(
                    center = pos,
                    radius = 50.0,
                    fillColor = when {
                        isCompleted -> Color(0x334CAF50)
                        isSelectedForRoute -> Color(0x33FFEB3B)
                        else -> Color(0x332196F3)
                    },
                    strokeColor = when {
                        isCompleted -> Color.Green
                        isSelectedForRoute -> Color.Yellow
                        else -> Color.Blue
                    },
                    strokeWidth = 2f
                )
            }

            // ── RUTAS ────────────────────────────────────────────────
            state.rutaPolylines.forEachIndexed { index, points ->
                if (points.size >= 2) {
                    Polyline(
                        points = points,
                        color = if (state.rutaMode == RutaMode.SEQUENTIAL) Color(0xFF4CAF50) else Color(0xFF2196F3),
                        width = 12f,
                        pattern = null
                    )
                }
            }

            // ── AMIGOS (fuera de carrera) ────────────────────────────
            if (state.activeSession == null) {
                state.friendLocations.forEach { friendLocation ->
                    Marker(
                        state = MarkerState(position = friendLocation.location),
                        title = friendLocation.user.displayName.ifBlank { "Amigo" },
                        snippet = friendLocation.user.gameId,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
            } else {
                // ── PARTICIPANTES (dentro de carrera) ────────────────
                state.participantLocations.forEach { participantLocation ->
                    Marker(
                        state = MarkerState(position = participantLocation.location),
                        title = participantLocation.displayName,
                        snippet = if (participantLocation.gameId.isNotEmpty()) {
                            participantLocation.gameId
                        } else {
                            "En carrera"
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                    )
                }
            }
        }

        // ── INDICADOR DE CARRERA ACTIVA ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            val session = state.activeSession
            val cardColor = when {
                session?.status == "active" -> Color(0xEEFF9800)
                session?.status == "lobby" -> Color(0xEE2A9D8F)
                else -> Color(0xEE333333)
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
                            session?.status == "active" -> "Carrera: ${session.raceName} · Participantes: ${state.participantLocations.size}"
                            session?.status == "lobby" -> "Lobby: ${session.raceName} (Esperando inicio...)"
                            else -> "No estás inscrito a ninguna carrera"
                        },
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── BOTONES DE RUTA (solo cuando hay carrera activa) ─────────
        if (state.activeSession?.status == "active") {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color(0xCC1A1A1A), RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Botón ruta secuencial
                    FilterChip(
                        selected = state.rutaMode == RutaMode.SEQUENTIAL,
                        onClick = {
                            if (state.rutaMode == RutaMode.SEQUENTIAL) {
                                viewModel.setRutaMode(RutaMode.NINGUNA)
                            } else {
                                viewModel.setRutaMode(RutaMode.SEQUENTIAL)
                            }
                        },
                        label = {
                            Text(
                                "Secuencial",
                                fontSize = 12.sp,
                                color = if (state.rutaMode == RutaMode.SEQUENTIAL) Color.Black else Color.White
                            )
                        },
                        leadingIcon = {
                            if (state.isLoadingRuta && state.rutaMode == RutaMode.SEQUENTIAL) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    Icons.Default.Navigation,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (state.rutaMode == RutaMode.SEQUENTIAL) Color.Black else Color.White
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF9800),
                            containerColor = Color(0xFF333333)
                        )
                    )

                    // Botón ruta a checkpoint individual
                    FilterChip(
                        selected = state.rutaMode == RutaMode.SINGLE,
                        onClick = {
                            if (state.rutaMode == RutaMode.SINGLE) {
                                viewModel.setRutaMode(RutaMode.NINGUNA)
                            } else {
                                viewModel.setRutaMode(RutaMode.SINGLE)
                            }
                        },
                        label = {
                            Text(
                                "Toca un punto",
                                fontSize = 12.sp,
                                color = if (state.rutaMode == RutaMode.SINGLE) Color.Black else Color.White
                            )
                        },
                        leadingIcon = {
                            if (state.isLoadingRuta && state.rutaMode == RutaMode.SINGLE) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    Icons.Default.TouchApp,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (state.rutaMode == RutaMode.SINGLE) Color.Black else Color.White
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF9800),
                            containerColor = Color(0xFF333333)
                        )
                    )
                }

                // Hint cuando modo SINGLE está activo y no hay checkpoint seleccionado
                if (state.rutaMode == RutaMode.SINGLE && state.selectedRouteCheckpoint == null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC1A1A1A))
                    ) {
                        Text(
                            text = "Toca un checkpoint para ver la ruta",
                            color = Color(0xFFFF9800),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Hint cuando modo SINGLE está activo y hay checkpoint seleccionado
                if (state.rutaMode == RutaMode.SINGLE && state.selectedRouteCheckpoint != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC1A1A1A))
                    ) {
                        Text(
                            text = "→ ${state.selectedRouteCheckpoint!!.name}",
                            color = Color(0xFF2196F3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ── BOTÓN TOGGLE DE UBICACIÓN ────────────────────────────────
        FloatingActionButton(
            onClick = { compartirUbicacion = !compartirUbicacion },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 180.dp),
            containerColor = if (compartirUbicacion) Color(0xFFFF9800) else Color(0xFF333333)
        ) {
            Icon(
                imageVector = if (compartirUbicacion) Icons.Default.LocationOn else Icons.Default.LocationOff,
                contentDescription = null,
                tint = Color.White
            )
        }

        // ── Widget podómetro (esquina inferior izquierda) ────────────
        StepWidget(
            steps = steps,
            isMoving = isMoving,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 120.dp)
        )

        // ── Panel inferior: validación de checkpoint ─────────────────
        // Solo se muestra si NO estamos en modo SINGLE (para no confundir al usuario)
        AnimatedVisibility(
            visible = selectedCheckpoint != null && state.rutaMode != RutaMode.SINGLE,
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
                val isCompleted = state.activeSession
                    ?.participants
                    ?.get(auth.currentUser?.uid)
                    ?.checkpointsDone
                    ?.contains(cp.id) == true

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
fun MapaSinPermiso(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.LocationOff, null, tint = Color(0xFFFF9800), modifier = Modifier.size(48.dp))
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