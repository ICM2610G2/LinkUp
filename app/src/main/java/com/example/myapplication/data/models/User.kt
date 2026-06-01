package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoURL: String = "",
    val gameId: String = "",
    val totalPlacesVisited: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalPoints: Int = 0,
    val shareLocationMode: String = "in_race",
    val fcmToken: String? = null,
    val createdAt: Timestamp = Timestamp.now()


)