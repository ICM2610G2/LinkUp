package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Race(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var createdBy: String = "",
    var coverImageUrl: String = "",
    var difficulty: String = "facil",
    var estimatedDistanceKm: Double = 0.0,
    var checkpointCount: Int = 0,
    @get:PropertyName("isPublic")
    @set:PropertyName("isPublic")
    var isPublic: Boolean = true,
    var createdAt: Timestamp = Timestamp.now(),
    var photoUrl: String = ""
)
