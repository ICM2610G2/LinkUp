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

data class CarrerasState(
    val raceRepository: RaceRepository = RaceRepository(),
    val activeSessions: List<RaceSession> = emptyList(),
    val otherRaces: List<Race> = emptyList(),
    val isLoading: Boolean = true,
)

class CarrerasViewModel : ViewModel() {
    private val _carrerasState = MutableStateFlow(CarrerasState())
    val carrerasState = _carrerasState.asStateFlow()

    fun cargarDatos() {
        viewModelScope.launch {
            _carrerasState.update { it.copy(isLoading = true) }
            val repo = _carrerasState.value.raceRepository
            
            val activeSessions = repo.getUserActiveSessions()
            Log.d("CarrerasViewModel", "Active sessions: $activeSessions")
            val allPublicRaces = repo.getPublicRaces()
            Log.d("CarrerasViewModel", "All public races: $allPublicRaces")

            val activeRaceIds = activeSessions.map { it.raceId }.toSet()
            val otherRaces = allPublicRaces.filter { it.id !in activeRaceIds }

            _carrerasState.update { 
                it.copy(
                    activeSessions = activeSessions,
                    otherRaces = otherRaces,
                    isLoading = false
                )
            }
        }
    }

    fun updateLoading(newValue: Boolean) {
        _carrerasState.update { it.copy(isLoading = newValue) }
    }
}
