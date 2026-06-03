package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class RaceSession(
    @DocumentId
    var id: String = "",
    var raceId: String = "",
    var raceName: String = "",
    var status: String = "lobby",
    var createdBy: String = "",
    var participants: Map<String, ParticipantInfo> = emptyMap(),
    var participantIds: List<String> = emptyList(),
    var startedAt: Timestamp? = null,
    var endedAt: Timestamp? = null,
    var winnerUid: String = "",
    var groupChatId: String = "",
    var createdAt: Timestamp = Timestamp.now()
)

@IgnoreExtraProperties
data class ParticipantInfo(
    var joinedAt: Timestamp? = null,
    var completedAt: Timestamp? = null,
    var lastCheckpointAt: Timestamp? = null,
    var position: Int? = null,
    var checkpointsDone: List<String> = emptyList(),
    var checkpointsPhotos: Map<String, String> = emptyMap()
)
