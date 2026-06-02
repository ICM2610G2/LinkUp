package com.example.myapplication.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Race
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.repository.RaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val mostrarCrearPunto: Boolean = false,
    val mostrarMenuFlotante: Boolean = false,
    val mostrarCrearCarrera: Boolean = false,
    val mostrarNFC: Boolean = false,
    val mostrarAmigos: Boolean = false,
    val lugarSeleccionado: String? = null,
    val publicRaces: List<Race> = emptyList(),
    val activeSession: RaceSession? = null,
    val isLoadingRaces: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()
    private val raceRepository = RaceRepository()

    fun cargarDatos() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoadingRaces = true) }
            try {
                // Cargar carreras públicas
                val races = raceRepository.getPublicRaces()
                
                // Cargar sesiones activas del usuario
                val activeSessions = raceRepository.getUserActiveSessions()
                val activeSession = activeSessions.firstOrNull()
                
                Log.d("HomeViewModel", "Datos cargados: ${races.size} rutas, sesión activa: ${activeSession?.id}")

                _homeState.update { it.copy(
                    publicRaces = races, 
                    activeSession = activeSession,
                    isLoadingRaces = false
                ) }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error cargando datos", e)
                _homeState.update { it.copy(isLoadingRaces = false) }
            }
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
