package com.example.myapplication.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val previewImage: Uri? = null,
    val fullScreenImage: Uri? = null,
    val currentUserPhotoURL: String = "",
    val currentUserDisplayName: String = "",
    val currentUserUid: String = ""
)

/**
 * ViewModel para la pantalla de Chat
 * Gestiona el estado de los mensajes y la carga de datos del usuario actual
 */
class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Cargar datos del usuario actual al iniciar el ViewModel
        loadCurrentUserData()
    }

    /**
     * Carga los datos del usuario actual desde Firestore y Firebase Auth
     * Actualiza el estado con la foto, nombre y UID para usar en los mensajes
     */
    private fun loadCurrentUserData() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            val userData = firebaseUser?.uid?.let { userRepository.getUser(it) }

            _chatState.update {
                it.copy(
                    currentUserPhotoURL = userData?.photoURL ?: firebaseUser?.photoUrl?.toString() ?: "",
                    currentUserDisplayName = userData?.displayName ?: firebaseUser?.displayName ?: "Tú",
                    currentUserUid = firebaseUser?.uid ?: ""
                )
            }
        }
    }

    /**
     * Actualiza el texto del campo de entrada
     * @param newValue Nuevo texto ingresado por el usuario
     */
    fun updateInputText(newValue: String) {
        _chatState.update { it.copy(inputText = newValue) }
    }

    /**
     * Actualiza la imagen en previsualización
     * @param newValue URI de la imagen seleccionada o null para limpiar
     */
    fun updatePreviewImage(newValue: Uri?) {
        _chatState.update { it.copy(previewImage = newValue) }
    }

    /**
     * Actualiza la imagen en pantalla completa
     * @param newValue URI de la imagen o null para cerrar
     */
    fun updateFullScreenImage(newValue: Uri?) {
        _chatState.update { it.copy(fullScreenImage = newValue) }
    }

    /**
     * Agrega un nuevo mensaje a la lista
     * Si el mensaje es del usuario actual, usa los datos cargados de Firestore
     * @param message Mensaje a agregar (puede ser creado manualmente o con los datos del estado)
     */
    fun addMessage(message: ChatMessage) {
        // Si el mensaje es del usuario actual y no tiene photoURL, usar la del estado
        val finalMessage = if (message.isMe && message.photoURL.isEmpty()) {
            val state = _chatState.value
            message.copy(
                photoURL = state.currentUserPhotoURL,
                sender = state.currentUserDisplayName,
                senderId = state.currentUserUid
            )
        } else {
            message
        }
        _chatState.update { it.copy(messages = it.messages + finalMessage) }
    }

    /**
     * Crea y envía un mensaje de texto desde el usuario actual
     * Usa los datos del usuario cargados en el estado (foto, nombre, UID)
     * @param text Texto del mensaje a enviar
     */
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        val state = _chatState.value
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val message = ChatMessage(
            id = state.messages.size + 1,
            sender = state.currentUserDisplayName,
            initial = state.currentUserDisplayName.take(2).uppercase(),
            text = text,
            senderId = state.currentUserUid,
            photoURL = state.currentUserPhotoURL,
            time = now,
            isMe = true
        )

        // Limpiar input después de enviar
        _chatState.update {
            it.copy(
                inputText = "",
                messages = it.messages + message
            )
        }
    }

    /**
     * Crea y envía un mensaje con imagen desde el usuario actual
     * @param imageUri URI de la imagen a enviar
     * @param caption Texto opcional que acompaña la imagen
     */
    fun sendImageMessage(imageUri: Uri, caption: String? = null) {
        val state = _chatState.value
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val message = ChatMessage(
            id = state.messages.size + 1,
            sender = state.currentUserDisplayName,
            initial = state.currentUserDisplayName.take(2).uppercase(),
            text = caption,
            imageUri = imageUri,
            senderId = state.currentUserUid,
            photoURL = state.currentUserPhotoURL,
            time = now,
            isMe = true
        )

        // Limpiar preview e input después de enviar
        _chatState.update {
            it.copy(
                previewImage = null,
                inputText = "",
                messages = it.messages + message
            )
        }
    }

    /**
     * Limpia la previsualización y el texto después de enviar
     */
    fun clearPreviewAndInput() {
        _chatState.update {
            it.copy(
                previewImage = null,
                inputText = ""
            )
        }
    }
}