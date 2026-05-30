package com.example.myapplication.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.LocationRepository
import com.example.myapplication.repository.UserRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*

data class FriendMapLocation(
    val user: User,
    val location: LatLng
)

@Composable
fun Mapa() {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (hasLocationPermission) {
        MapaConUbicacion()
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
fun MapaConUbicacion() {
    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val userRepository = remember { UserRepository() }
    val friendsRepository = remember { FriendsRepository() }
    val locationRepository = remember { LocationRepository() }
    val realtimeDb = remember { FirebaseDatabase.getInstance().reference }

    var shareLocationMode by remember { mutableStateOf("always") }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var acceptedFriends by remember { mutableStateOf<List<User>>(emptyList()) }
    var friendLocations by remember { mutableStateOf<List<FriendMapLocation>>(emptyList()) }

    val bogota = LatLng(4.6097, -74.0817)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 13f)
    }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(3000L)
            .build()
    }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        val user = userRepository.getUser(uid)
        shareLocationMode = user?.shareLocationMode ?: "always"

        acceptedFriends = friendsRepository.getAcceptedFriends()
    }

    DisposableEffect(Unit) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val latLng = LatLng(location.latitude, location.longitude)
                userLocation = latLng

                val shouldShare = shareLocationMode == "always" || shareLocationMode == "in_race"

                locationRepository.updateUserLocation(
                    lat = location.latitude,
                    lng = location.longitude,
                    visible = shouldShare
                )
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        onDispose {
            locationRepository.removeLocation()
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    DisposableEffect(acceptedFriends) {
        val listeners = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()

        acceptedFriends.forEach { friend ->
            val ref = realtimeDb
                .child("user_live")
                .child(friend.uid)
                .child("location")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val visible = snapshot.child("visible").getValue(Boolean::class.java) ?: false
                    val lat = snapshot.child("lat").getValue(Double::class.java)
                    val lng = snapshot.child("lng").getValue(Double::class.java)

                    friendLocations = if (visible && lat != null && lng != null) {
                        val newLocation = FriendMapLocation(
                            user = friend,
                            location = LatLng(lat, lng)
                        )

                        friendLocations
                            .filterNot { it.user.uid == friend.uid } + newLocation
                    } else {
                        friendLocations.filterNot { it.user.uid == friend.uid }
                    }
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

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(location, 16f)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            )
        ) {
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Mi ubicación",
                    snippet = "Estás aquí"
                )
            }

            friendLocations.forEach { friendLocation ->
                Marker(
                    state = MarkerState(position = friendLocation.location),
                    title = friendLocation.user.displayName.ifBlank { "Amigo" },
                    snippet = friendLocation.user.gameId,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_ORANGE
                    )
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE1A1A1A))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (userLocation == null) {
                        "Obteniendo ubicación..."
                    } else {
                        "Ubicación activa · Amigos visibles: ${friendLocations.size}"
                    },
                    color = Color.White
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Permiso de ubicación requerido",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "LinkUp necesita tu ubicación para mostrarte en el mapa, validar puntos y participar en carreras.",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Activar ubicación")
                }
            }
        }
    }
}