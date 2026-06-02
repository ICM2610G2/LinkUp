package com.example.myapplication.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Friendship(
    @DocumentId
    val id: String = "",
    val userA: String = "",           // uid del que envía
    val userB: String = "",           // uid del que recibe
    val status: String = "pending",   // pendiente, aceptada , rechazada
    val requestedAt: Timestamp = Timestamp.now(),
    val acceptedAt: Timestamp? = null
) {
    // Función para generar ID único independiente del orden
    companion object {
        fun generateId(uid1: String, uid2: String): String {
            return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
        }
    }
}