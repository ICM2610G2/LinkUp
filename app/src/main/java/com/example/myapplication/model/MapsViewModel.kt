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
    val isLoading: Boolean = false
)

class MapaViewModel : ViewModel() {
    private val _mapaState = MutableStateFlow(MapaState())
    val mapaState = _mapaState.asStateFlow()
    private val raceRepository = RaceRepository()
    private val friendsRepository = FriendsRepository()

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

    fun cargarDatos() {
        viewModelScope.launch {
            _mapaState.update { it.copy(isLoading = true) }
            
            val sessions = raceRepository.getUserActiveSessions()
            val current = sessions.find { it.status == "active" || it.status == "lobby" }
            
            if (current?.status == "active") {
                // CASO 1: Carrera iniciada -> Mostrar Checkpoints, Ocultar Amigos
                val checkpoints = raceRepository.getCheckpoints(current.raceId)
                _mapaState.update { it.copy(
                    activeSession = current, 
                    checkpoints = checkpoints,
                    acceptedFriends = emptyList(),
                    friendLocations = emptyList()
                ) }
            } else {
                // CASO 2: En Lobby o Sin Carrera -> Mostrar Amigos, Ocultar Checkpoints
                val amigos = friendsRepository.getAcceptedFriends()
                _mapaState.update { it.copy(
                    activeSession = current, // Podría ser null o lobby
                    checkpoints = emptyList(),
                    acceptedFriends = amigos
                ) }
            }
            
            _mapaState.update { it.copy(isLoading = false) }
        }
    }
}
