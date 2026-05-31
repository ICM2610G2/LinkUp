package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class RaceSession(
    @DocumentId
    val id: String = "",
    val raceId: String = "",
    val raceName: String = "",
    val status: String = "lobby",
    val createdBy: String = "",
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp? = null,
    val winnerUid: String = "",
    val groupChatId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)