package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.RaceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LobbyState(
    val raceName: String = "Cargando carrera...",
    val raceId: String = "",
    val status: String = "lobby",
    val participantsCount: Int = 0,
    val createdBy: String = "",
    val isParticipant: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val raceStarted: Boolean = false,
    val leftRace: Boolean = false
)

class LobbyViewModel : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state = _state.asStateFlow()

    private val raceRepository = RaceRepository()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var sessionListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startListening(sessionId: String) {
        sessionListener?.remove()
        sessionListener = FirebaseFirestore.getInstance()
            .collection("race_sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val participants = snapshot.get("participants") as? Map<*, *>
                    val newStatus = snapshot.getString("status") ?: "lobby"
                    val isParticipant = participants?.containsKey(currentUid) == true

                    _state.update {
                        it.copy(
                            raceName = snapshot.getString("raceName") ?: "Carrera",
                            raceId = snapshot.getString("raceId") ?: "",
                            status = newStatus,
                            createdBy = snapshot.getString("createdBy") ?: "",
                            participantsCount = participants?.size ?: 0,
                            isParticipant = isParticipant,
                            raceStarted = newStatus == "active" && isParticipant
                        )
                    }
                }
            }
    }

    fun joinRace(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = raceRepository.joinRaceSession(sessionId)
            result.onFailure { e ->
                _state.update { it.copy(errorMessage = e.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun startRace(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = raceRepository.startRaceSession(sessionId)
            result.onFailure { e ->
                _state.update { it.copy(errorMessage = e.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun leaveRace(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = raceRepository.leaveRaceSession(sessionId)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, leftRace = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
            )
        }
    }

    fun clearRaceStarted() {
        _state.update { it.copy(raceStarted = false) }
    }

    override fun onCleared() {
        super.onCleared()
        sessionListener?.remove()
    }
}