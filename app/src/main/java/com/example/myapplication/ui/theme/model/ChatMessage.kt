package com.example.myapplication.ui.theme.model

import android.net.Uri

data class ChatMessage(
    val id: Int,
    val sender: String,
    val initial: String,
    val text: String? = null,
    val imageUri: Uri? = null,   // ✅ NEW (for gallery images)
    val time: String,
    val isMe: Boolean,
    val isImageVerified: Boolean = false
)