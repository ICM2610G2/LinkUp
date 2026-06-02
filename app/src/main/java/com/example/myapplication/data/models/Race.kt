package com.example.myapplication.data.models


import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Race(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val coverImageUrl: String = "",
    val difficulty: String = "facil",
    val estimatedDistanceKm: Double = 0.0,
    val checkpointCount: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val photoUrl: String = ""
)