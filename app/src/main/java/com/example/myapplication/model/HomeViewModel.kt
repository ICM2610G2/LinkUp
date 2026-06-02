package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Race
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.repository.RaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeSessionItem(
    val session: RaceSession,
    val race: Race?
)

data class HomeState(
    val recentSessions: List<HomeSessionItem> = emptyList(),
    val isLoadingSessions: Boolean = false,
    val mostrarCrearPunto: Boolean = false,
    val mostrarMenuFlotante: Boolean = false,
    val mostrarCrearCarrera: Boolean = false,
    val mostrarNFC: Boolean = false,
    val mostrarAmigos: Boolean = false,
    val lugarSeleccionado: String? = null
)

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()
    private val raceRepository = RaceRepository()

    fun cargarRecentSessions() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoadingSessions = true) }
            val sessions = raceRepository.getUserActiveSessions()
            
            // Get the 3 most recent sessions based on createdAt
            val mostRecent = sessions.sortedByDescending { it.createdAt }.take(3)
            
            // Fetch associated race data for each session to show photos/details
            val sessionItems = mostRecent.map { session ->
                val race = raceRepository.getRaceById(session.raceId)
                HomeSessionItem(session, race)
            }
            
            _homeState.update { it.copy(recentSessions = sessionItems, isLoadingSessions = false) }
        }
    }

    fun updateMostrarCrearPunto(newValue: Boolean) {
        _homeState.update { it.copy(mostrarCrearPunto = newValue) }
    }
    fun updateMostrarMenuFlotante(newValue: Boolean) {
        _homeState.update { it.copy(mostrarMenuFlotante = newValue) }
    }
    fun updateMostrarCrearCarrera(newValue: Boolean) {
        _homeState.update { it.copy(mostrarCrearCarrera = newValue) }
    }
    fun updateMostrarNFC(newValue: Boolean) {
        _homeState.update { it.copy(mostrarNFC = newValue) }
    }
    fun updateMostrarAmigos(newValue: Boolean) {
        _homeState.update { it.copy(mostrarAmigos = newValue) }
    }
    fun updateLugarSeleccionado(newValue: String?) {
        _homeState.update { it.copy(lugarSeleccionado = newValue) }
    }
}
