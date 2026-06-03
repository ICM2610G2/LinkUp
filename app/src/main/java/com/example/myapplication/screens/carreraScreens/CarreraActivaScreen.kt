package com.example.myapplication.screens.carreraScreens

import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.model.WikipediaViewModel
import com.example.myapplication.repository.RaceRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun CarreraActivaScreen(
    sessionId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val wikipediaViewModel: WikipediaViewModel = viewModel()
    val wikipediaState by wikipediaViewModel.state.collectAsState()

    var raceId by remember { mutableStateOf("") }
    var raceName by remember { mutableStateOf("Carrera activa") }
    var createdBy by remember { mutableStateOf("") }
    var participantsCount by remember { mutableStateOf(0) }
    var myCheckpointsDone by remember { mutableStateOf<List<String>>(emptyList()) }
    var checkpoints by remember { mutableStateOf<List<Checkpoint>>(emptyList()) }
    var status by remember { mutableStateOf("active") }

    // Estados de ubicación y validación
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isUploading by remember { mutableStateOf(false) }
    var pendingCheckpoint by remember { mutableStateOf<Checkpoint?>(null) }

    // Launcher para seleccionar/tomar foto
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && pendingCheckpoint != null) {
            scope.launch {
                isUploading = true
                val result = raceRepository.uploadCheckpointPhoto(
                    sessionId = sessionId,
                    checkpointId = pendingCheckpoint!!.id,
                    imageUri = uri
                )
                result.fold(
                    onSuccess = {
                        Toast.makeText(context, "¡Punto validado!", Toast.LENGTH_SHORT).show()
                        wikipediaViewModel.buscarLugar(pendingCheckpoint!!.name)
                        pendingCheckpoint = null
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
                isUploading = false
            }
        }
    }

    // Seguimiento de ubicación
    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                userLocation = result.lastLocation?.let { LatLng(it.latitude, it.longitude) }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("CarreraActiva", "Sin permisos de ubicación", e)
        }
        onDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    DisposableEffect(sessionId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("race_sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    raceId = snapshot.getString("raceId") ?: ""
                    raceName = snapshot.getString("raceName") ?: "Carrera activa"
                    createdBy = snapshot.getString("createdBy") ?: ""
                    status = snapshot.getString("status") ?: "active"

                    val participants = snapshot.get("participants") as? Map<*, *>
                    participantsCount = participants?.size ?: 0

                    val myData = participants?.get(uid) as? Map<*, *>
                    myCheckpointsDone =
                        myData?.get("checkpointsDone") as? List<String> ?: emptyList()

                    if (status == "finished") {
                        onBack()
                    }
                }
            }
        onDispose { listener.remove() }
    }

    LaunchedEffect(raceId) {
        if (raceId.isNotBlank()) {
            checkpoints = raceRepository.getCheckpoints(raceId)
        }
    }

    val completedCount = myCheckpointsDone.size
    val total = checkpoints.size
    val progress = if (total == 0) 0f else completedCount.toFloat() / total.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Text(
                    raceName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoBadgeCarrera("${participantsCount} jugadores", Icons.Default.Groups)
                    InfoBadgeCarrera("$completedCount/$total puntos", Icons.Default.Flag)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFF252525)
                )
            }

            items(checkpoints, key = { it.id }) { checkpoint ->
                val done = myCheckpointsDone.contains(checkpoint.id)

                CheckpointCarreraItem(
                    checkpoint = checkpoint,
                    done = done,
                    userLocation = userLocation,
                    onValidate = {
                        pendingCheckpoint = checkpoint
                        photoLauncher.launch("image/*")
                    }
                )
            }

            if (uid == createdBy) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                raceRepository.finishRaceSession(sessionId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.SportsScore, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finalizar Carrera", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Overlay de carga
        if (isUploading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        }

        // Wikipedia Dialogs...
        if (wikipediaState.isLoading) {
            Card(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Consultando Wikipedia...", color = Color.White)
                }
            }
        }

        wikipediaState.place?.let { place ->
            CulturalRewardDialog(
                title = place.title,
                extract = place.extract,
                imageUrl = place.imageUrl,
                onClose = { wikipediaViewModel.limpiar() }
            )
        }
    }
}

@Composable
fun InfoBadgeCarrera(
    text: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.White)
    }
}

@Composable
fun CheckpointCarreraItem(
    checkpoint: Checkpoint,
    done: Boolean,
    userLocation: LatLng?,
    onValidate: () -> Unit
) {
    // Cálculo de distancia
    val results = FloatArray(1)
    if (userLocation != null) {
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            checkpoint.coordinates.latitude, checkpoint.coordinates.longitude,
            results
        )
    }
    val distance = if (userLocation != null) results[0] else Float.MAX_VALUE
    val isWithinRange = distance <= 50f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (done) Color(0xFF22C55E) else Color(0x12FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (done) Color(0xFF22C55E) else Color(0x26FF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (done) Icons.Default.Check else Icons.Default.Place,
                    null,
                    tint = if (done) Color.White else Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(checkpoint.name, color = Color.White, fontWeight = FontWeight.Bold)
                if (!done) {
                    Text(
                        text = if (userLocation != null) "Distancia: ${distance.toInt()}m" else "Calculando...",
                        color = if (isWithinRange) Color.Green else Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                }
                Text(checkpoint.description, color = Color.Gray, maxLines = 2, fontSize = 13.sp)
            }

            Button(
                onClick = onValidate,
                enabled = !done && isWithinRange,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    disabledContainerColor = Color(0xFF252525)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (done) {
                    Text("Listo")
                } else {
                    Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Validar")
                }
            }
        }
    }
}

@Composable
fun CulturalRewardDialog(
    title: String,
    extract: String,
    imageUrl: String?,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            border = BorderStroke(1.dp, Color(0x33FF9800))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lugar descubierto",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF252525), RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = extract,
                    color = Color(0xFFBDBDBD),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "+100 XP",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continuar carrera")
                }
            }
        }
    }
}
