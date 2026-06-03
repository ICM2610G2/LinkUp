package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Friendship
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SolicitudesState(
    val solicitudes: List<SolicitudConUsuario> = emptyList(),
    val isLoading: Boolean = true
)

data class SolicitudConUsuario(
    val friendship: Friendship,
    val solicitante: User
)

class SolicitudesViewModel : ViewModel() {
    private val _state = MutableStateFlow(SolicitudesState())
    val state = _state.asStateFlow()
    private val friendsRepository = FriendsRepository()
    private val userRepository = UserRepository()
    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val pending = friendsRepository.getPendingRequests()
            val enriched = pending.mapNotNull { friendship ->
                val user = userRepository.getUser(friendship.userA)
                if (user != null) SolicitudConUsuario(friendship, user) else null
            }
            _state.update { it.copy(solicitudes = enriched, isLoading = false) }
        }
    }

    fun acceptRequest(id: String) {
        viewModelScope.launch {
            friendsRepository.acceptRequest(id)
            loadRequests()
        }
    }

    fun rejectRequest(id: String) {
        viewModelScope.launch {
            friendsRepository.rejectRequest(id)
            loadRequests()
        }
    }
}