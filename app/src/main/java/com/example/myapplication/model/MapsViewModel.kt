package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.RaceRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

enum class RutaMode { NINGUNA, SEQUENTIAL, SINGLE }

data class FriendMapLocation(
    val user: User,
    val location: LatLng
)

data class ParticipantMapLocation(
    val uid: String,
    val displayName: String,
    val gameId: String = "",
    val location: LatLng
)

data class MapaState(
    val hasLocationPermission: Boolean = false,
    val shareLocationMode: String = "always",
    val userLocation: LatLng? = null,
    val acceptedFriends: List<User> = emptyList(),
    val friendLocations: List<FriendMapLocation> = emptyList(),
    val participantLocations: List<ParticipantMapLocation> = emptyList(),
    val activeSession: RaceSession? = null,
    val checkpoints: List<Checkpoint> = emptyList(),
    val isLoading: Boolean = false,
    val rutaMode: RutaMode = RutaMode.NINGUNA,
    val rutaPolylines: List<List<LatLng>> = emptyList(),
    val isLoadingRuta: Boolean = false,
    val selectedRouteCheckpoint: Checkpoint? = null
)

class MapaViewModel : ViewModel() {
    private val _mapaState = MutableStateFlow(MapaState())
    val mapaState = _mapaState.asStateFlow()
    private val raceRepository = RaceRepository()
    private val friendsRepository = FriendsRepository()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val API_KEY = "AIzaSyB60y55WfRS-T06rpBSpKgrjqGVEZPZZgw"
    }

    fun updateHasLocationPermission(newValue: Boolean) {
        _mapaState.update { it.copy(hasLocationPermission = newValue) }
    }

    fun updateShareLocationMode(newValue: String) {
        _mapaState.update { it.copy(shareLocationMode = newValue) }
    }

    fun updateUserLocation(newValue: LatLng?) {
        _mapaState.update { it.copy(userLocation = newValue) }
    }

    fun updateAcceptedFriends(newValue: List<User>) {
        _mapaState.update { it.copy(acceptedFriends = newValue) }
    }

    fun updateFriendLocations(newValue: List<FriendMapLocation>) {
        _mapaState.update { it.copy(friendLocations = newValue) }
    }

    fun updateParticipantLocations(newValue: List<ParticipantMapLocation>) {
        _mapaState.update { it.copy(participantLocations = newValue) }
    }

    fun updateActiveSession(newValue: RaceSession?) {
        _mapaState.update { it.copy(activeSession = newValue) }
    }

    fun updateCheckpoints(newValue: List<Checkpoint>) {
        _mapaState.update { it.copy(checkpoints = newValue) }
        val mode = _mapaState.value.rutaMode
        if (mode == RutaMode.SEQUENTIAL && newValue.isNotEmpty()) {
            calcularRutas(mode)
        }
    }

    fun setRutaMode(mode: RutaMode) {
        _mapaState.update {
            it.copy(
                rutaMode = mode,
                rutaPolylines = emptyList(),
                selectedRouteCheckpoint = null
            )
        }
        if (mode == RutaMode.SEQUENTIAL) {
            calcularRutas(mode)
        }
        // SINGLE no calcula hasta que el usuario toque un checkpoint
    }

    fun selectRouteCheckpoint(checkpoint: Checkpoint) {
        if (_mapaState.value.rutaMode != RutaMode.SINGLE) return
        _mapaState.update { it.copy(selectedRouteCheckpoint = checkpoint) }
        calcularRutas(RutaMode.SINGLE)
    }

    fun calcularRutas(mode: RutaMode) {
        val userLocation = _mapaState.value.userLocation ?: return
        val checkpoints = _mapaState.value.checkpoints
        if (checkpoints.isEmpty()) return

        viewModelScope.launch {
            _mapaState.update { it.copy(isLoadingRuta = true) }
            val polylines = mutableListOf<List<LatLng>>()

            try {
                when (mode) {
                    RutaMode.SEQUENTIAL -> {
                        val uid = auth.currentUser?.uid ?: ""
                        val session = _mapaState.value.activeSession
                        val done = session?.participants?.get(uid)?.checkpointsDone ?: emptyList()

                        // Construir lista ordenada: siempre elegir el más cercano al punto anterior
                        val pending = checkpoints.filter { !done.contains(it.id) }.toMutableList()
                        val ordered = mutableListOf<Checkpoint>()
                        var currentPos = userLocation

                        while (pending.isNotEmpty()) {
                            val nearest = pending.minByOrNull { cp ->
                                distanceBetween(
                                    currentPos,
                                    LatLng(cp.coordinates.latitude, cp.coordinates.longitude)
                                )
                            }!!
                            ordered.add(nearest)
                            pending.remove(nearest)
                            currentPos = LatLng(nearest.coordinates.latitude, nearest.coordinates.longitude)
                        }

                        // Trazar ruta encadenada: userLocation -> cp1 -> cp2 -> ...
                        var origin = userLocation
                        ordered.forEach { cp ->
                            val dest = LatLng(cp.coordinates.latitude, cp.coordinates.longitude)
                            val points = getDirectionsPolyline(origin, dest)
                            if (points.isNotEmpty()) polylines.add(points)
                            origin = dest
                        }
                    }

                    RutaMode.SINGLE -> {
                        val target = _mapaState.value.selectedRouteCheckpoint ?: return@launch
                        val dest = LatLng(target.coordinates.latitude, target.coordinates.longitude)
                        val points = getDirectionsPolyline(userLocation, dest)
                        if (points.isNotEmpty()) polylines.add(points)
                    }

                    RutaMode.NINGUNA -> {}
                }
            } catch (e: Exception) {
                // Fallback a líneas rectas si falla la API
                when (mode) {
                    RutaMode.SEQUENTIAL -> {
                        val uid = auth.currentUser?.uid ?: ""
                        val session = _mapaState.value.activeSession
                        val done = session?.participants?.get(uid)?.checkpointsDone ?: emptyList()
                        val pending = checkpoints.filter { !done.contains(it.id) }.toMutableList()
                        val ordered = mutableListOf<Checkpoint>()
                        var currentPos = userLocation

                        while (pending.isNotEmpty()) {
                            val nearest = pending.minByOrNull { cp ->
                                distanceBetween(
                                    currentPos,
                                    LatLng(cp.coordinates.latitude, cp.coordinates.longitude)
                                )
                            }!!
                            ordered.add(nearest)
                            pending.remove(nearest)
                            currentPos = LatLng(nearest.coordinates.latitude, nearest.coordinates.longitude)
                        }

                        var origin = userLocation
                        ordered.forEach { cp ->
                            val dest = LatLng(cp.coordinates.latitude, cp.coordinates.longitude)
                            polylines.add(listOf(origin, dest))
                            origin = dest
                        }
                    }
                    RutaMode.SINGLE -> {
                        val target = _mapaState.value.selectedRouteCheckpoint ?: return@launch
                        polylines.add(
                            listOf(
                                userLocation,
                                LatLng(target.coordinates.latitude, target.coordinates.longitude)
                            )
                        )
                    }
                    RutaMode.NINGUNA -> {}
                }
            }

            _mapaState.update { it.copy(rutaPolylines = polylines, isLoadingRuta = false) }
        }
    }

    private suspend fun getDirectionsPolyline(origin: LatLng, dest: LatLng): List<LatLng> {
        return try {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${dest.latitude},${dest.longitude}" +
                    "&mode=walking" +
                    "&key=$API_KEY"

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                URL(url).readText()
            }

            val json = JSONObject(response)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return emptyList()

            val points = routes.getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")

            decodePolyline(points)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    private fun distanceBetween(a: LatLng, b: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude, a.longitude,
            b.latitude, b.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _mapaState.update { it.copy(isLoading = true) }

            val sessions = raceRepository.getUserActiveSessions()
            val current = sessions.find { it.status == "active" || it.status == "lobby" }

            if (current?.status == "active") {
                val checkpoints = raceRepository.getCheckpoints(current.raceId)
                _mapaState.update {
                    it.copy(
                        activeSession = current,
                        checkpoints = checkpoints,
                        acceptedFriends = emptyList(),
                        friendLocations = emptyList()
                    )
                }
            } else {
                val amigos = friendsRepository.getAcceptedFriends()
                _mapaState.update {
                    it.copy(
                        activeSession = current,
                        checkpoints = emptyList(),
                        acceptedFriends = amigos
                    )
                }
            }

            _mapaState.update { it.copy(isLoading = false) }
        }
    }

    fun cargarCarreraActiva() {
        cargarDatos()
    }
}