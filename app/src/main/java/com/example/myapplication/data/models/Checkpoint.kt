package com.example.myapplication.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Checkpoint(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val coordinates: GeoPoint = GeoPoint(0.0, 0.0),
    val geofenceRadiusM: Int = 50,
    val points: Int = 100,
    val photoUrl: String = "",
    val order: Int = 0
)