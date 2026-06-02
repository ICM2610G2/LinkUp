package com.example.myapplication.model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration

data class ChatDetailState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = true
)

class ChatDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state.asStateFlow()

    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private var currentChatId: String = ""
    private var isReadOnly: Boolean = false
    private var listenerRegistration: ListenerRegistration? = null

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }


    suspend fun getChatParticipants(chatId: String): List<String> {
        return try {
            val chatDoc = chatRepository.getChatById(chatId)
            chatDoc?.participants ?: emptyList()
        } catch (e: Exception) {
            Log.e("ChatDetailVM", "Error getting participants: ${e.message}")
            emptyList()
        }
    }

    fun initChat(chatId: String, readOnly: Boolean) {
        currentChatId = chatId
        isReadOnly = readOnly
        Log.d("ChatDetailVM", "Chat inicializado: $chatId, readOnly: $readOnly")
    }

    fun updateInputText(text: String) {
        _state.update { it.copy(inputText = text) }
        Log.d("ChatDetailVM", "Input text actualizado: $text")
    }

    fun loadMessages(chatId: String) {
        Log.d("ChatDetailVM", "Cargando mensajes para chat: $chatId")
        _state.update { it.copy(isLoading = true) }

        listenerRegistration?.remove()
        listenerRegistration = chatRepository.listenToMessages(chatId) { messages ->
            Log.d("ChatDetailVM", "Mensajes recibidos: ${messages.size}")
            _state.update {
                it.copy(
                    messages = messages,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Envía un mensaje de texto al chat actual
     * Usa el chatId que se guardó en initChat
     */
    fun sendTextMessage() {
        val text = _state.value.inputText
        val chatId = currentChatId

        Log.d("ChatDetailVM", "sendTextMessage llamado - chatId: $chatId, text: '$text', isReadOnly: $isReadOnly")

        if (text.isBlank()) {
            Log.d("ChatDetailVM", "Texto vacío, no se envía")
            return
        }

        if (isReadOnly) {
            Log.d("ChatDetailVM", "Chat en modo solo lectura, no se envía")
            return
        }

        if (chatId.isEmpty()) {
            Log.d("ChatDetailVM", "Chat ID vacío, no se envía")
            return
        }

        Log.d("ChatDetailVM", "Enviando mensaje: $text")

        viewModelScope.launch {
            try {
                val result = chatRepository.sendTextMessage(chatId, text)
                result.fold(
                    onSuccess = {
                        Log.d("ChatDetailVM", "Mensaje enviado exitosamente")
                        _state.update { it.copy(inputText = "") }
                    },
                    onFailure = { e ->
                        Log.e("ChatDetailVM", "Error al enviar mensaje: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("ChatDetailVM", "Excepción al enviar: ${e.message}")
            }
        }
    }

    /**
     * Envía un mensaje de texto a un chat específico
     * @param chatId ID del chat (override)
     */
    fun sendTextMessage(chatId: String) {
        // Actualizar el chatId actual si es diferente
        if (currentChatId != chatId) {
            currentChatId = chatId
        }
        sendTextMessage()
    }

    fun sendImageMessage(chatId: String, imageUri: Uri) {
        if (isReadOnly) return

        Log.d("ChatDetailVM", "Enviando imagen a chat $chatId")

        viewModelScope.launch {
            try {
                val result = chatRepository.sendImageMessage(chatId, imageUri)
                result.fold(
                    onSuccess = { Log.d("ChatDetailVM", "Imagen enviada exitosamente") },
                    onFailure = { e -> Log.e("ChatDetailVM", "Error al enviar imagen: ${e.message}") }
                )
            } catch (e: Exception) {
                Log.e("ChatDetailVM", "Excepción al enviar imagen: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
        Log.d("ChatDetailVM", "ViewModel limpiado")
    }
}