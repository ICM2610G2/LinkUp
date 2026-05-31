package com.example.myapplication.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val previewImage: Uri? = null,
    val fullScreenImage: Uri? = null
)

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    fun updateInputText(newValue: String) {
        _chatState.update { it.copy(inputText = newValue) }
    }
    fun updatePreviewImage(newValue: Uri?) {
        _chatState.update { it.copy(previewImage = newValue) }
    }
    fun updateFullScreenImage(newValue: Uri?) {
        _chatState.update { it.copy(fullScreenImage = newValue) }
    }
    fun addMessage(message: ChatMessage) {
        _chatState.update { it.copy(messages = it.messages + message) }
    }
}