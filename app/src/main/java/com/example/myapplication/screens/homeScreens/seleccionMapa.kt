package com.example.myapplication.screens.homeScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun SeleccionMapa(
    onPuntoSeleccionado: (Double, Double) -> Unit,
    onCerrar: () -> Unit
) {
    var puntoSeleccionado by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6097, -74.0817), 15f)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapClick = { latLng ->
                puntoSeleccionado = latLng
            }
        ) {
            puntoSeleccionado?.let { punto ->
                Marker(
                    state = MarkerState(position = punto),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCerrar,
                modifier = Modifier.background(Color(0xCC1A1A1A), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }

            Text(
                "Selecciona un punto",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xCC1A1A1A), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        // FAB de confirmación
        if (puntoSeleccionado != null) {
            ExtendedFloatingActionButton(
                onClick = {
                    onPuntoSeleccionado(
                        puntoSeleccionado!!.latitude,
                        puntoSeleccionado!!.longitude
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                containerColor = Color(0xFF2A9D8F),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar ubicación")
            }
        }
    }
}