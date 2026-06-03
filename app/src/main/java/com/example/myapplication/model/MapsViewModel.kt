package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.RaceRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendMapLocation(
    val user: User,
    val location: LatLng
)

data class MapaState(
    val hasLocationPermission: Boolean = false,
    val shareLocationMode: String = "always",
    val userLocation: LatLng? = null,
    val acceptedFriends: List<User> = emptyList(),
    val friendLocations: List<FriendMapLocation> = emptyList(),
    val activeSession: RaceSession? = null,
    val checkpoints: List<Checkpoint> = emptyList(),
    val isLoadingActiveRace: Boolean = false
)

class MapaViewModel : ViewModel() {
    private val _mapaState = MutableStateFlow(MapaState())
    val mapaState = _mapaState.asStateFlow()
    private val raceRepository = RaceRepository()

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

    fun cargarCarreraActiva() {
        viewModelScope.launch {
            _mapaState.update { it.copy(isLoadingActiveRace = true) }
            val sessions = raceRepository.getUserActiveSessions()
            // Buscamos la primera sesión que esté "active"
            val active = sessions.find { it.status == "active" }

            if (active != null) {
                val checkpoints = raceRepository.getCheckpoints(active.raceId)
                _mapaState.update { it.copy(activeSession = active, checkpoints = checkpoints, isLoadingActiveRace = false) }
            } else {
                _mapaState.update { it.copy(activeSession = null, checkpoints = emptyList(), isLoadingActiveRace = false) }
            }
        }
    }
}
