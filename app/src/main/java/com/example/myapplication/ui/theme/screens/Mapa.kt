package com.example.myapplication.ui.theme.screens

import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Looper
import android.util.Log
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
    //("Not yet implemented")
}

//@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Mapa(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = viewModel()
) {
    LocationPermission()

    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var place by remember { mutableStateOf("") }
    var isDarkMode by remember { mutableStateOf(true) }

    // Sensor de luminosidad
    val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                Log.i("MapApp", lux.toString())
                isDarkMode = lux < 20.0f
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    Location { viewModel.onLocationUpdate(it) }

    var mostrarValidarFoto by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // ── Mapa OSM ──────────────────────────────────────────────────────────
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

                // Marcador del usuario (índice 0 en el tag)
                val userMarker = Marker(mapView)
                userMarker.position = startPoint
                // tag: [userMarker, searchMarker?, longClickMarker?, destinoMarkers...]
                mapView.tag = mutableListOf<Any?>(userMarker, null, null)
                mapView.overlays.add(userMarker)

                // Marcadores de destinos fijos
                viewModel.destinos.forEach { destino ->
                    val m = Marker(mapView)
                    m.position = destino.punto
                    m.title = destino.nombre
                    // Icono personalizado si tienes uno, si no usa el por defecto
                    // m.icon = ContextCompat.getDrawable(ctx, R.drawable.ic_destino)
                    m.setOnMarkerClickListener { _, _ ->
                        viewModel.onDestinoClick(destino)
                        true   // consumimos el click → no muestra el label por defecto
                    }
                    mapView.overlays.add(m)
                }

                // Long-click en el mapa
                val overlayEvents = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        //p?.let { viewModel.onLongClick(GeoPoint(it.latitude, it.longitude)) }
                        return true
                    }
                })
                mapView.overlays.add(overlayEvents)
                mapView
            },
            update = { mapView ->
                //@Suppress("UNCHECKED_CAST")
                val tags = mapView.tag as MutableList<Any?>
                val userMarker = tags[0] as Marker
                var changed = false

                // Modo oscuro/claro
                if (isDarkMode) {
                    val inverse = ColorMatrix(floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(inverse))
                } else {
                    mapView.overlayManager.tilesOverlay.setColorFilter(null)
                }
                changed = true

                // Posición del usuario
                state.userLocation?.let {
                    if (userMarker.position != it) {
                        userMarker.position = it
                        userMarker.title = state.userAddress
                        mapView.controller.setCenter(it)
                        changed = true
                    }
                }

                // Marcador de búsqueda
                /*state.searchedLocation?.let { loc ->
                    val searchMarker = tags[1] as? Marker
                        ?: Marker(mapView).also {
                            mapView.overlays.add(it)
                            tags[1] = it
                        }
                    if (searchMarker.position != loc) {
                        searchMarker.position = loc
                        searchMarker.title = state.searchedAddress
                        mapView.controller.setCenter(loc)
                        changed = true
                    }
                }*/

                // Marcador de long-click
                /*state.longClickLocation?.let { loc ->
                    val longClickMarker = tags[2] as? Marker
                        ?: Marker(mapView).also {
                            mapView.overlays.add(it)
                            tags[2] = it
                        }
                    if (longClickMarker.position != loc) {
                        longClickMarker.position = loc
                        longClickMarker.title = state.longClickAddress
                        changed = true
                    }
                }*/

                // Overlay de ruta
                state.roadOverlay?.let { overlay ->
                    mapView.overlays.removeIf { it is Polyline }
                    mapView.overlays.add(overlay)
                    changed = true
                }

                // Si no hay overlay activo, limpia polylines
                if (state.roadOverlay == null) {
                    if (mapView.overlays.any { it is Polyline }) {
                        mapView.overlays.removeIf { it is Polyline }
                        changed = true
                    }
                }

                if (changed) mapView.invalidate()
            }
        )

        // ── DestinoCard: visible solo cuando hay destino seleccionado ─────────
        if (state.selectedDestino != null) {
            DestinoCard(
                destino = state.selectedDestino!!,
                distanciaMetros = state.distanciaAlDestino,
                onCerrar = { viewModel.onCerrarDestino() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, end = 92.dp, top = 14.dp)
            )
        }

        // ── Botones laterales ─────────────────────────────────────────────────
        BotonesLaterales(
            mostrandoRutaCompleta = state.mostrandoRutaCompleta,
            onToggleRutaCompleta = { viewModel.onToggleRutaCompleta() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 14.dp)
        )

        // ── CardInferior: visible solo cuando hay destino seleccionado ────────
        if (state.selectedDestino != null) {
            CardInferior(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 10.dp, end = 14.dp, bottom = 20.dp),
                onValidarFoto = {
                    Log.i("MyApp", "Validar foto")
                    mostrarValidarFoto = true
                }
            )
        }

        if (mostrarValidarFoto) {
            ValidarFoto(
                nombreLugar = state.selectedDestino?.nombre ?: "Destino",
                onCerrar = { mostrarValidarFoto = false },
                onConfirmar = {
                    Log.i("MyApp", "Llegada confirmada: ${state.selectedDestino?.nombre}")
                    mostrarValidarFoto = false
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DestinoCard — ahora recibe datos dinámicos del destino seleccionado
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// BotonesLaterales — último botón activa/desactiva ruta completa
// ─────────────────────────────────────────────────────────────────────────────
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
        Card(
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
        }

        // Brújula
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.End).clickable { Log.i("MyApp", "Brújula clicked") },
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

        // ── Botón ruta completa (ÚLTIMO botón) ────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// CardInferior — sin cambios en su estructura interna
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CardInferior(modifier: Modifier = Modifier, onValidarFoto: () -> Unit) {
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

            Row(
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onValidarFoto,
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
