package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.User
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FriendMapLocation(
    val user: User,
    val location: LatLng
)
data class MapaState(
    val hasLocationPermission: Boolean = false,
    val shareLocationMode: String = "always",
    val userLocation: LatLng? = null,
    val acceptedFriends: List<User> = emptyList(),
    val friendLocations: List<FriendMapLocation> = emptyList()
)

class MapaViewModel : ViewModel() {
    private val _mapaState = MutableStateFlow(MapaState())
    val mapaState = _mapaState.asStateFlow()

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
}