package com.example.myapplication.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseTest {
    fun testConnection(onResult: (Boolean, String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val testDoc = hashMapOf(
            "test" to "connection",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("_test_connection")
            .add(testDoc)
            .addOnSuccessListener {
                onResult(true, "Firebase conectado correctamente")
            }
            .addOnFailureListener { e ->
                onResult(false, "Error: ${e.message}")
            }
    }
}