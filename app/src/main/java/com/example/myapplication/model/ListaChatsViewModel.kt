package com.example.myapplication.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.Chat
import com.example.myapplication.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.firestore.ListenerRegistration

data class ListaChatsState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true,
    val currentUserId: String = ""
)

class ListaChatsViewModel : ViewModel() {
    private val _state = MutableStateFlow(ListaChatsState())
    val state = _state.asStateFlow()

    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    init {
        Log.d("ListaChatsVM", "ViewModel iniciado")
        loadChats()
    }

    fun loadChats() {
        try {
            val userId = auth.currentUser?.uid ?: ""
            Log.d("ListaChatsVM", "userId: $userId")

            _state.update {
                it.copy(
                    currentUserId = userId,
                    isLoading = true
                )
            }

            // Remover listener anterior si existe
            listenerRegistration?.remove()

            // Escuchar cambios en tiempo real
            listenerRegistration = chatRepository.listenToChats { chats ->
                Log.d("ListaChatsVM", "Chats recibidos: ${chats.size}")
                _state.update {
                    it.copy(
                        chats = chats,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ListaChatsVM", "Error en loadChats: ${e.message}", e)
            _state.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ListaChatsVM", "ViewModel limpiado")
        listenerRegistration?.remove()
    }
}