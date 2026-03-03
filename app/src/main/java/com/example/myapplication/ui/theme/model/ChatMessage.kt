package com.example.myapplication.ui.theme.model

import androidx.annotation.DrawableRes

data class ChatMessage(
    val id: Int,
    val sender: String,
    val text: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val time: String,
    val isMe: Boolean = false,
    val isImageVerified: Boolean = false
)