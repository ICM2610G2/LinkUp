package com.example.myapplication.ui.theme.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.model.Destino
import com.example.myapplication.ui.theme.model.MapsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import kotlin.math.sqrt
import kotlin.math.abs

val cameraPerm = Manifest.permission.CAMERA
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission() {
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (locationPermissionState.status.isGranted) {
        GPSContents()
    }
}

@Composable
fun GPSContents() {
    /*...*/
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Mapa(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = viewModel()
) {

    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // PERMISSIONS
    val activityPermission = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    SideEffect { activityPermission.launchPermissionRequest() }

    // UI STATE
    var isDarkMode by remember { mutableStateOf(true) }
    var mostrarValidarFoto by remember { mutableStateOf(false) }

    // SENSOR STATE
    var stepCount by remember { mutableStateOf(0) }
    var isWalking by remember { mutableStateOf(false) }

    var accelMagnitude by remember { mutableStateOf(0f) }
    var lastAccel by remember { mutableStateOf(0f) }
    var smoothedAccel by remember { mutableStateOf(0f) }

    var stepCooldown by remember { mutableStateOf(0L) }
    var lastCheckTime by remember { mutableStateOf(0L) }

    // SENSORS
    val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            when (event?.sensor?.type) {

                Sensor.TYPE_LIGHT -> {
                    val lux = event.values[0]
                    isDarkMode = lux < 20f
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val magnitude = sqrt(x * x + y * y + z * z)
                    accelMagnitude = magnitude

                    val delta = abs(magnitude - lastAccel)
                    lastAccel = magnitude

                    // LOW PASS FILTER
                    smoothedAccel = smoothedAccel * 0.8f + delta * 0.2f

                    val now = System.currentTimeMillis()

                    // CHECK ONLY EVERY 1 SECOND
                    if (now - lastCheckTime > 1000) {

                        // MORE SENSITIVE WALK DETECTION
                        isWalking = smoothedAccel > 0.6f

                        // STEP DETECTION (fallback)
                        if (stepDetector == null && smoothedAccel > 1.2f) {
                            stepCount++
                        }

                        lastCheckTime = now
                    }

                    // COOLDOWN STEP DETECTION
                    if (stepDetector == null) {
                        if (delta > 1.8f && (now - stepCooldown) > 500) {
                            stepCount++
                            stepCooldown = now
                        }
                    }
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    stepCount++
                    isWalking = true
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        stepDetector?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    Location { viewModel.onLocationUpdate(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {

        AndroidView(
            modifier = modifier.matchParentSize(),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName

                val mapView = MapView(ctx)
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.controller.setZoom(18.0)

                val startPoint = GeoPoint(4.627293, -74.063228)
                mapView.controller.setCenter(startPoint)

                val userMarker = Marker(mapView)
                userMarker.position = startPoint
                mapView.tag = mutableListOf<Any?>(userMarker, null, null)
                mapView.overlays.add(userMarker)

                viewModel.destinos.forEach { destino ->
                    val m = Marker(mapView)
                    m.position = destino.punto
                    m.title = destino.nombre
                    m.setOnMarkerClickListener { _, _ ->
                        viewModel.onDestinoClick(destino)
                        true
                    }
                    mapView.overlays.add(m)
                }

                val overlayEvents = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                    override fun longPressHelper(p: GeoPoint?) = true
                })
                mapView.overlays.add(overlayEvents)

                mapView
            },
            update = { mapView ->
                val tags = mapView.tag as MutableList<Any?>
                val userMarker = tags[0] as Marker
                var changed = false

                if (isDarkMode) {
                    val inverse = ColorMatrix(floatArrayOf(
                        -1f,0f,0f,0f,255f,
                        0f,-1f,0f,0f,255f,
                        0f,0f,-1f,0f,255f,
                        0f,0f,0f,1f,0f
                    ))
                    mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(inverse))
                } else {
                    mapView.overlayManager.tilesOverlay.setColorFilter(null)
                }

                state.userLocation?.let {
                    if (userMarker.position != it) {
                        userMarker.position = it
                        mapView.controller.setCenter(it)
                        changed = true
                    }
                }

                state.roadOverlay?.let {
                    mapView.overlays.removeIf { o -> o is Polyline }
                    mapView.overlays.add(it)
                    changed = true
                }

                if (state.roadOverlay == null) {
                    if (mapView.overlays.any { it is Polyline }) {
                        mapView.overlays.removeIf { it is Polyline }
                        changed = true
                    }
                }

                if (changed) mapView.invalidate()
            }
        )

        if (state.selectedDestino != null) {
            DestinoCard(
                destino = state.selectedDestino!!,
                distanciaMetros = state.distanciaAlDestino,
                onCerrar = { viewModel.onCerrarDestino() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, end = 112.dp, top = 10.dp)
            )
        }

        BotonesLaterales(
            mostrandoRutaCompleta = state.mostrandoRutaCompleta,
            onToggleRutaCompleta = { viewModel.onToggleRutaCompleta() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 14.dp)
        )

        if (state.selectedDestino != null) {
            CardInferior(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp),
                onValidarFoto = { mostrarValidarFoto = true }
            )
        }

        if (mostrarValidarFoto) {
            ValidarFoto(
                nombreLugar = state.selectedDestino?.nombre ?: "Destino",
                onCerrar = { mostrarValidarFoto = false },
                onConfirmar = { mostrarValidarFoto = false }
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Actividad", color = Color.White.copy(0.6f))
                Text(
                    if (isWalking) "Caminando 🚶" else "Quieto 🧍",
                    color = if (isWalking) Color(0xFF22C55E) else Color.Gray
                )
                Text("Pasos: $stepCount", color = Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun DestinoCard(
    destino: Destino,
    distanciaMetros: Double?,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Formatea la distancia en metros o km
    val distTexto = when {
        distanciaMetros == null -> "—"
        distanciaMetros < 1000 -> "${distanciaMetros.toInt()} m"
        else -> "${"%.1f".format(distanciaMetros / 1000)} km"
    }
    // Estimación de tiempo caminando (~5 km/h = 83 m/min)
    val minutos = if (distanciaMetros != null) (distanciaMetros / 83).toInt().coerceAtLeast(1) else null

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Encabezado con título y botón cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("DESTINO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                //Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.weight(75f)) {
                    // Nombre del destino
                    Text(
                        destino.nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Descripción breve
                    Text(
                        destino.descripcion,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }

                // Imagen del sitio
                Image(
                    painter = painterResource(id = destino.imagenRes),
                    contentDescription = destino.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(25f)
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Distancia y tiempo estimado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFF9800), CircleShape)
                    )
                    Text(distTexto, color = Color(0xFFFF9800), fontSize = 12.sp)
                }
                if (minutos != null) {
                    Text("~ $minutos min", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BotonesLaterales(
    mostrandoRutaCompleta: Boolean,
    onToggleRutaCompleta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón estado del usuario (caminando / validado)
        /*Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
            modifier = Modifier.align(Alignment.End)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF22C55E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }
                Text("Caminando", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("Validado ✓", color = Color(0xFF22C55E), fontSize = 8.sp)
            }
        }*/

        // Brújula
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.End).padding(top = 100.dp).clickable { Log.i("MyApp", "Brújula clicked") },
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(Icons.Default.Explore, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
            }
        }

        // Capas
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.End).clickable { Log.i("MyApp", "Capas clicked") },
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(Icons.Default.Layers, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Boton ruta
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (mostrandoRutaCompleta) Color(0xFFFF9800) else Color(0xF21A1A1A)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.End).clickable { onToggleRutaCompleta() },
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (mostrandoRutaCompleta) Color(0xFFFF9800) else Color(0x1AFFFFFF)
            )
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = "Ruta completa",
                    tint = if (mostrandoRutaCompleta) Color.White else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CardInferior(modifier: Modifier = Modifier, onValidarFoto: () -> Unit) {
    val context = LocalContext.current
    val permissionStatus = rememberPermissionState(cameraPerm)
    var imageUri by remember{ mutableStateOf<Uri?>(null) }
    val cameraUri = FileProvider.getUriForFile(context,
        "${context.packageName}.fileprovider",
        File(context.filesDir, "cameraPic.jpg")
    )

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()){ it ->
        if(it){
            imageUri = cameraUri
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Brújula", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("N 23° NE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                VisualizacionBrujula()
            }

            Spacer(modifier = Modifier.height(12.dp))

            /*Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Velocidad", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("5.2 km/h", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Movimiento", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Activo ✓", color = Color(0xFF22C55E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))*/

            Button(
                onClick = {
                    onValidarFoto
                    if(!permissionStatus.status.isGranted){
                        permissionStatus.launchPermissionRequest()
                    }
                    if(permissionStatus.status.isGranted ){
                        camera.launch(cameraUri)
                    }else{
                        imageUri = null
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validar llegada con foto", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun VisualizacionBrujula() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .drawBehind {
                val cx = size.width / 2
                val cy = size.height / 2
                drawCircle(
                    color = Color(0x4DFF9800),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = size.minDimension / 2 - 8.dp.toPx(),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = Color(0xFFFF9800),
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - 18.dp.toPx()),
                    strokeWidth = 3f
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text("N", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Location(onLocationUpdate: (GeoPoint) -> Unit) {
    val context = LocalContext.current
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val permission = rememberPermissionState(locationPermission)

    SideEffect {
        permission.launchPermissionRequest()
    }

    val locationRequest = remember { createLocationRequest() }

    if (permission.status.isGranted) {
        val locationCallback = remember {
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        onLocationUpdate(GeoPoint(loc.latitude, loc.longitude))
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED) {
                locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
            onDispose { locationClient.removeLocationUpdates(locationCallback) }
        }
    }
}

fun createLocationRequest(): LocationRequest {
    return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(5000)
        .build()
}

@Preview
@Composable
fun mapaPreview() {
    //BotonesLaterales(true, viewModel())
    //CardInferior { }
}
