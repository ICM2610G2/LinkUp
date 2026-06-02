package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId


data class Chat(
    @DocumentId
    val id: String = "",
    val type: String = "direct", // "direct" o "group"
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: Timestamp = Timestamp.now(),
    val sessionId: String = "",   // Solo para chats de grupo
    val readOnly: Boolean = false,
    val deleteAt: Timestamp? = null, // Solo para chats grupales
    val createdAt: Timestamp = Timestamp.now()
)