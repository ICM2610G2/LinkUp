package com.example.myapplication.model

import android.net.Uri

data class ChatMessage(
    val id: Int,
    val sender: String,
    val initial: String,
    val text: String? = null,
    val imageUri: Uri? = null,
    val senderId: String = "",
    val photoURL: String = "",
    val time: String,
    val isMe: Boolean,
    val isImageVerified: Boolean = false
)