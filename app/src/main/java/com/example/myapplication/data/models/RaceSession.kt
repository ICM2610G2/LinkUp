package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class RaceSession(
    val id: String = "",
    val raceId: String = "",
    val raceName: String = "",
    val status: String = "lobby",
    val createdBy: String = "",
    val participants: Map<String, ParticipantInfo> = emptyMap(),
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp? = null,
    val winnerUid: String = "",
    val groupChatId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class ParticipantInfo(
    val joinedAt: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val position: Int? = null,
    val checkpointsDone: List<String> = emptyList()
)
