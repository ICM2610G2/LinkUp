package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.ChatRepository
import com.example.myapplication.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitarAmigosState(
    val amigos: List<User> = emptyList(),
    val seleccionados: Set<String> = emptySet(), // UIDs seleccionados
    val isLoading: Boolean = true,
    val isEnviando: Boolean = false,
    val enviado: Boolean = false
)

class InvitarAmigosViewModel : ViewModel() {
    private val _state = MutableStateFlow(InvitarAmigosState())
    val state = _state.asStateFlow()

    private val friendsRepository = FriendsRepository()
    private val chatRepository = ChatRepository()

    fun cargarAmigos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val amigos = friendsRepository.getAcceptedFriends()
            _state.update { it.copy(amigos = amigos, isLoading = false) }
        }
    }

    fun toggleSeleccion(uid: String) {
        _state.update { current ->
            val nuevos = if (current.seleccionados.contains(uid)) {
                current.seleccionados - uid
            } else {
                current.seleccionados + uid
            }
            current.copy(seleccionados = nuevos)
        }
    }

    fun enviarInvitaciones(raceName: String, raceId: String) {
        val seleccionados = _state.value.seleccionados
        if (seleccionados.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isEnviando = true) }
            val mensaje = "🏃 Te han invitado a una carrera\n\nNombre: $raceName\nID: $raceId"
            for (uid in seleccionados) {
                val chatResult = chatRepository.getOrCreateDirectChat(uid)
                chatResult.onSuccess { chat ->
                    chatRepository.sendTextMessage(chat.id, mensaje)
                }
            }
            _state.update { it.copy(isEnviando = false, enviado = true) }
        }
    }

    fun resetEnviado() {
        _state.update { it.copy(enviado = false, seleccionados = emptySet()) }
    }
}