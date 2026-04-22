package com.example.myapplication.ui.theme.model

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

// ---------- Modelo de un destino fijo ----------
data class Destino(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val punto: GeoPoint,
    val imagenRes: Int          // drawable resource id
)

// ---------- Destinos quemados en código ----------
val DESTINOS_FIJOS = listOf(
    Destino(
        id = 1,
        nombre = "Plaza de Bolívar",
        descripcion = "Centro histórico de Bogotá",
        punto = GeoPoint(4.5981, -74.0758),
        imagenRes = R.drawable.ic_launcher_background   // reemplaza con tu drawable real
    ),
    Destino(
        id = 2,
        nombre = "Monserrate",
        descripcion = "Cerro icónico con vista panorámica",
        punto = GeoPoint(4.6058, -74.0557),
        imagenRes = R.drawable.ic_launcher_background
    ),
    Destino(
        id = 3,
        nombre = "Museo del Oro",
        descripcion = "La colección de orfebrería precolombina más grande del mundo",
        punto = GeoPoint(4.6016, -74.0724),
        imagenRes = R.drawable.ic_launcher_background
    ),
    Destino(
        id = 4,
        nombre = "La Candelaria",
        descripcion = "Barrio colonial lleno de arte y cultura",
        punto = GeoPoint(4.5960, -74.0730),
        imagenRes = R.drawable.ic_launcher_background
    )
)

// ---------- Estado del mapa ----------
data class MapsState(
    val userLocation: GeoPoint? = null,
    val searchedLocation: GeoPoint? = null,
    val longClickLocation: GeoPoint? = null,
    val showHistory: Boolean = false,
    val userAddress: String = "",
    val longClickAddress: String = "",
    val searchedAddress: String = "",
    val distMessage: String? = null,
    val locationHistory: List<GeoPoint> = emptyList(),
    val roadOverlay: Polyline? = null,

    // Destino seleccionado al tocar un marcador
    val selectedDestino: Destino? = null,
    // Distancia en metros al destino seleccionado
    val distanciaAlDestino: Double? = null,
    // true = mostrar ruta por todos los destinos
    val mostrandoRutaCompleta: Boolean = false
)

class MapsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(MapsState())
    val state: StateFlow<MapsState> = _state.asStateFlow()
    private val geocoder = Geocoder(app)
    private val roadManager = OSRMRoadManager(app, "ANDROID")

    // Lista pública de destinos para que el composable pueda usarla
    val destinos: List<Destino> = DESTINOS_FIJOS

    fun onLocationUpdate(point: GeoPoint) {
        _state.update { it.copy(userLocation = point) }
        val addr = findAddress(point) ?: "Ubicación desconocida"
        _state.update { it.copy(userAddress = addr) }

        // Actualizar distancia si hay un destino seleccionado
        _state.value.selectedDestino?.let { dest ->
            val dist = distance(
                point.latitude, point.longitude,
                dest.punto.latitude, dest.punto.longitude
            )
            _state.update { it.copy(distanciaAlDestino = dist) }
        }

        if (!_state.value.mostrandoRutaCompleta) findRoute()
    }

    fun onLongClick(point: GeoPoint) {
        _state.update { it.copy(longClickLocation = point) }
        val addr = findAddress(point) ?: "Ubicación desconocida"
        _state.update { it.copy(longClickAddress = addr) }
        if (!_state.value.mostrandoRutaCompleta) findRoute()
    }

    fun onSearch(address: String) {
        val place = geocoder.getFromLocationName(address, 2)
        if (place != null && place.isNotEmpty()) {
            val point = GeoPoint(place[0].latitude, place[0].longitude)
            _state.update {
                it.copy(
                    searchedLocation = point,
                    searchedAddress = place[0].getAddressLine(0) ?: address
                )
            }
            if (!_state.value.mostrandoRutaCompleta) findRoute()
        }
    }

    /** Llamado cuando el usuario toca un marcador de destino fijo */
    fun onDestinoClick(destino: Destino) {
        val user = _state.value.userLocation
        val dist = if (user != null) {
            distance(user.latitude, user.longitude, destino.punto.latitude, destino.punto.longitude)
        } else null

        _state.update {
            it.copy(
                selectedDestino = destino,
                distanciaAlDestino = dist,
                mostrandoRutaCompleta = false
            )
        }

        // Trazar ruta solo a este destino
        if (user != null) {
            viewModelScope.launch {
                val points = arrayListOf(user, destino.punto)
                val road = roadManager.getRoad(points)
                val overlay = RoadManager.buildRoadOverlay(road)
                overlay?.outlinePaint?.strokeWidth = 10f
                _state.update { it.copy(roadOverlay = overlay) }
            }
        }
    }

    /** Cierra el panel de destino seleccionado */
    fun onCerrarDestino() {
        _state.update { it.copy(selectedDestino = null, distanciaAlDestino = null, roadOverlay = null) }
    }

    /** Alterna la ruta que pasa por TODOS los destinos */
    fun onToggleRutaCompleta() {
        val activa = _state.value.mostrandoRutaCompleta
        if (activa) {
            _state.update {
                it.copy(mostrandoRutaCompleta = false, roadOverlay = null, selectedDestino = null)
            }
        } else {
            _state.update { it.copy(mostrandoRutaCompleta = true, selectedDestino = null) }
            val user = _state.value.userLocation ?: return
            viewModelScope.launch {
                val points = ArrayList<GeoPoint>().apply {
                    add(user)
                    addAll(destinos.map { it.punto })
                }
                val road = roadManager.getRoad(points)
                val overlay = RoadManager.buildRoadOverlay(road)
                overlay?.outlinePaint?.strokeWidth = 10f
                overlay?.outlinePaint?.color = android.graphics.Color.parseColor("#FF9800")
                _state.update { it.copy(roadOverlay = overlay) }
            }
        }
    }

    private fun findAddress(location: GeoPoint): String? {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
        if (addresses != null && addresses.isNotEmpty()) {
            return addresses[0].getAddressLine(0)
        }
        return null
    }

    private fun findRoute() {
        val user = _state.value.userLocation ?: return
        _state.value.searchedLocation ?: _state.value.longClickLocation ?: return

        viewModelScope.launch {
            val points = arrayListOf(user)
            _state.value.longClickLocation?.let { points.add(it) }
            _state.value.searchedLocation?.let { points.add(it) }

            val road = roadManager.getRoad(points)
            val overlay = RoadManager.buildRoadOverlay(road)
            overlay?.outlinePaint?.strokeWidth = 10f
            _state.update { it.copy(roadOverlay = overlay) }
        }
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat1 - lat2)
        val dLon = Math.toRadians(lon1 - lon2)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371 * c * 1000
    }
}
