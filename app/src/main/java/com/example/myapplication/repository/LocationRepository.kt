package com.example.myapplication.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LocationRepository {

    private val auth = FirebaseAuth.getInstance()

    private val db = FirebaseDatabase.getInstance(
        "https://linkup-99296-default-rtdb.firebaseio.com/"
    ).reference

    fun updateUserLocation(
        lat: Double,
        lng: Double,
        visible: Boolean
    ) {
        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "lat" to lat,
            "lng" to lng,
            "updatedAt" to System.currentTimeMillis(),
            "visible" to visible
        )

        db.child("user_live")
            .child(uid)
            .child("location")
            .setValue(data)
    }

    fun removeLocation() {
        val uid = auth.currentUser?.uid ?: return

        db.child("user_live")
            .child(uid)
            .removeValue()
    }
}