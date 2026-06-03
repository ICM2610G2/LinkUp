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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.model.FriendMapLocation
import com.example.myapplication.model.MapaViewModel
<<<<<<< Updated upstream
=======
import com.example.myapplication.model.ParticipantMapLocation
import com.example.myapplication.repository.FriendsRepository
>>>>>>> Stashed changes
import com.example.myapplication.repository.LocationRepository
import com.example.myapplication.repository.RaceRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

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

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.updateHasLocationPermission(granted)

        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Cargamos tanto la carrera como los amigos según el estado
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

                    Log.d("MAP_SESSION", " Sesión activa detectada: ${activeSession?.id}")
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
    // UBICACIÓN EN TIEMPO REAL - ACTUALIZA live_positions Y user_live
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

                Log.d("MAP_LOCATION", " Ubicación: lat=${location.latitude}, lng=${location.longitude}")
                Log.d("MAP_LOCATION", "activeSessionId: $activeSessionId, compartirUbicacion: $compartirUbicacion")

                if (compartirUbicacion) {
                    if (activeSessionId != null) {
                        Log.d("MAP_LOCATION", " Escribiendo en live_positions/$activeSessionId/$uid")
                        locationRepository.updateRaceLocation(
                            sessionId = activeSessionId,
                            lat = location.latitude,
                            lng = location.longitude
                        )
                    } else {
                        Log.d("MAP_LOCATION", " Escribiendo en user_live/$uid")
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
                Log.d("MAP_LOCATION", " LocationAvailability: ${availability.isLocationAvailable}")
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

<<<<<<< Updated upstream
    // Lógica de amigos (Realtime DB) - Se activa solo si hay amigos aceptados en el estado
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
                        state.friendLocations.filterNot { it.user.uid == friend.uid } + FriendMapLocation(friend, LatLng(lat, lng))
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

=======
    // ============================================================
    // FORZAR ESCRITURA INICIAL CUANDO SE DETECTA SESIÓN ACTIVA
    // ============================================================
    LaunchedEffect(state.activeSession, state.userLocation) {
        val session = state.activeSession
        val userLocation = state.userLocation

        Log.d("MAP_SESSION_CHECK", " Verificando: session=$session, userLocation=$userLocation, compartir=$compartirUbicacion")

        if (session != null && userLocation != null && compartirUbicacion) {
            Log.d("MAP_SESSION_CHECK", " Forzando escritura inicial en live_positions")
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
        Log.d("MAP_PARTICIPANTS", " Escuchando participantes para sesión: $sessionId")

        if (sessionId == null) {
            viewModel.updateParticipantLocations(emptyList())
            onDispose { }
        } else {
            val ref = realtimeDb.child("live_positions").child(sessionId)
            Log.d("MAP_PARTICIPANTS", "📡 Ruta: $ref")

            // Mapa para almacenar información de los participantes (nombre y gameId)
            val participantsInfo = mutableMapOf<String, Triple<String, String, Boolean>>()

            // Cargar información de los participantes desde Firestore
            scope.launch {
                val session = state.activeSession
                session?.participantIds?.forEach { uid ->
                    val user = userRepository.getUser(uid)
                    if (user != null) {
                        participantsInfo[uid] = Triple(user.displayName, user.gameId, false)
                        Log.d("MAP_PARTICIPANTS", " Cargado: $uid -> ${user.displayName}, ${user.gameId}")
                    }
                }

                // Marcar como cargados
                participantsInfo.keys.forEach { uid ->
                    participantsInfo[uid] = participantsInfo[uid]?.copy(third = true) ?: Triple("", "", true)
                }
            }

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("MAP_PARTICIPANTS", " DataSnapshot recibido. Children: ${snapshot.childrenCount}")

                    val updatedList = snapshot.children.mapNotNull { child ->
                        val uid = child.key ?: return@mapNotNull null
                        val lat = child.child("lat").getValue(Double::class.java)
                        val lng = child.child("lng").getValue(Double::class.java)

                        Log.d("MAP_PARTICIPANTS", "   - Procesando uid: $uid, lat=$lat, lng=$lng")

                        if (lat != null && lng != null) {
                            val userInfo = participantsInfo[uid]

                            // Para el usuario actual, obtener datos directamente
                            val displayName = if (uid == auth.currentUser?.uid) {
                                "Tú"
                            } else {
                                userInfo?.first ?: "Participante"
                            }


                            val gameId = if (uid == auth.currentUser?.uid) {
                                "Tú"
                            } else {
                                userInfo?.second ?: ""
                            }

                            Log.d("MAP_PARTICIPANTS", "   - Participante: $uid, nombre=$displayName, gameId=$gameId")

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

                    Log.d("MAP_PARTICIPANTS", " Participantes encontrados: ${updatedList.size}")
                    viewModel.updateParticipantLocations(updatedList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MAP_PARTICIPANTS", " Error: ${error.message}")
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
>>>>>>> Stashed changes
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true)
        ) {
<<<<<<< Updated upstream
            // Checkpoints (Solo aparecen si la carrera está iniciada)
=======
            // ============================================================
            // CHECKPOINTS
            // ============================================================
>>>>>>> Stashed changes
            state.checkpoints.forEach { checkpoint ->
                val pos = LatLng(checkpoint.coordinates.latitude, checkpoint.coordinates.longitude)
                val isCompleted = state.activeSession
                    ?.participants
                    ?.get(auth.currentUser?.uid)
                    ?.checkpointsDone
                    ?.contains(checkpoint.id) == true

                Marker(
                    state = MarkerState(position = pos),
                    title = checkpoint.name,
                    snippet = if (isCompleted) "✓ Completado" else "Pendiente",
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

<<<<<<< Updated upstream
            // Amigos (Solo aparecen si no hay carrera activa según el ViewModel)
            state.friendLocations.forEach { friendLocation ->
                Marker(
                    state = MarkerState(position = friendLocation.location),
                    title = friendLocation.user.displayName,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // --- PANEL SUPERIOR: Nombre de Carrera, Lobby o Aviso ---
=======
            // ============================================================
            // AMIGOS (fuera de carrera) - Color NARANJA
            // ============================================================
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
                // ============================================================
                // PARTICIPANTES (dentro de carrera) - Color ROSADO con NOMBRE y GAME ID
                // ============================================================
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

        // ============================================================
        // INDICADOR DE CARRERA ACTIVA
        // ============================================================
>>>>>>> Stashed changes
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            val session = state.activeSession
            val cardColor = when {
                session?.status == "active" -> Color(0xEEFF9800) // Naranja para carrera
                session?.status == "lobby" -> Color(0xEE2A9D8F)  // Teal para lobby
                else -> Color(0xEE333333)                        // Gris para aviso
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
<<<<<<< Updated upstream
                        text = when {
                            session?.status == "active" -> "Carrera: ${session.raceName}"
                            session?.status == "lobby" -> "Lobby: ${session.raceName} (Esperando inicio...)"
                            else -> "No estás inscrito a ninguna carrera. ¡Inscríbete a una!"
                        },
                        color = textColor,
=======
                        text = state.activeSession?.let {
                            "Carrera: ${it.raceName} · Participantes: ${state.participantLocations.size}"
                        } ?: "No estás inscrito a ninguna carrera",
                        color = if (state.activeSession != null) Color.Black else Color.White,
>>>>>>> Stashed changes
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ============================================================
        // BOTÓN TOGGLE DE UBICACIÓN
        // ============================================================
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

        // ============================================================
        // PANEL DE VALIDACIÓN DE CHECKPOINT
        // ============================================================
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
                Text("Ubicación requerida", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Para participar en carreras y ver amigos, activa los permisos.", color = Color.Gray, fontSize = 14.sp)
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
