package com.example.myapplication.repository

import android.util.Log
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
            .addOnSuccessListener {
                Log.d("LocationRepo", " user_live/$uid/location actualizado")
            }
            .addOnFailureListener { e ->
                Log.e("LocationRepo", " Error actualizando user_live: ${e.message}")
            }
    }

    fun updateRaceLocation(
        sessionId: String,
        lat: Double,
        lng: Double
    ) {
        val uid = auth.currentUser?.uid ?: return

        Log.d("LocationRepo", "🏁 updateRaceLocation: sessionId=$sessionId, uid=$uid, lat=$lat, lng=$lng")

        val data = mapOf(
            "lat" to lat,
            "lng" to lng,
            "updatedAt" to System.currentTimeMillis()
        )

        db.child("live_positions")
            .child(sessionId)
            .child(uid)
            .setValue(data)
            .addOnSuccessListener {
                Log.d("LocationRepo", " live_positions/$sessionId/$uid actualizado")
            }
            .addOnFailureListener { e ->
                Log.e("LocationRepo", " Error actualizando live_positions: ${e.message}")
            }
    }

    fun removeRaceLocation(sessionId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.child("live_positions")
            .child(sessionId)
            .child(uid)
            .removeValue()
            .addOnSuccessListener {
                Log.d("LocationRepo", "🗑️ live_positions/$sessionId/$uid eliminado")
            }
    }

    fun removeLocation() {
        val uid = auth.currentUser?.uid ?: return
        db.child("user_live")
            .child(uid)
            .removeValue()
            .addOnSuccessListener {
                Log.d("LocationRepo", "🗑️ user_live/$uid eliminado")
            }
    }
}