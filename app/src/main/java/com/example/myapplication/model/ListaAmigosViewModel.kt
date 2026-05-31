package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.FriendsRepository
import com.example.myapplication.repository.UserRepository
//import com.example.myapplication.screens.SolicitudConUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaAmigosState(
    val amigos: List<User> = emptyList(),
    val solicitudes: List<SolicitudConUsuario> = emptyList(),
    val isLoading: Boolean = true,
    val tabActiva: String = "todos"
)

class ListaAmigosViewModel : ViewModel() {
    private val _state = MutableStateFlow(ListaAmigosState())
    val state = _state.asStateFlow()
    private val friendsRepository = FriendsRepository()
    private val userRepository = UserRepository()
    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val amigos = friendsRepository.getAcceptedFriends()
            val pending = friendsRepository.getPendingRequests()
            val solicitudes = pending.mapNotNull { friendship ->
                val user = userRepository.getUser(friendship.userA)
                if (user != null) SolicitudConUsuario(friendship, user) else null
            }
            _state.update { it.copy(
                amigos = amigos,
                solicitudes = solicitudes,
                isLoading = false
            )}
        }
    }

    fun updateTabActiva(newValue: String) {
        _state.update { it.copy(tabActiva = newValue) }
    }

    fun removeAmigo(uid: String) {
        viewModelScope.launch {
            friendsRepository.removeFriend(uid)
            loadAll()
        }
    }

    fun blockAmigo(uid: String) {
        viewModelScope.launch {
            friendsRepository.blockUser(uid)
            loadAll()
        }
    }

    fun acceptRequest(id: String) {
        viewModelScope.launch {
            friendsRepository.acceptRequest(id)
            loadAll()
        }
    }

    fun rejectRequest(id: String) {
        viewModelScope.launch {
            friendsRepository.rejectRequest(id)
            loadAll()
        }
    }
}