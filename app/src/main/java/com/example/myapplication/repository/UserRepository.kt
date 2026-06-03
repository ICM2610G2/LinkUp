package com.example.myapplication.repository

import com.example.myapplication.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun getUser(uid: String): User? {
        return try {
            val docSnapshot = firestore.collection("users").document(uid).get().await()
            docSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentUser(): User? {
        val currentUid = auth.currentUser?.uid ?: return null
        return getUser(currentUid)
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(uid: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("displayName", newName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhotoURL(uid: String, photoURL: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("photoURL", photoURL)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShareLocationMode(uid: String, mode: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("shareLocationMode", mode)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStats(
        uid: String,
        totalPlacesVisited: Int? = null,
        currentStreak: Int? = null,
        bestStreak: Int? = null,
        totalPoints: Int? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            totalPlacesVisited?.let { updates["totalPlacesVisited"] = it }
            currentStreak?.let { updates["currentStreak"] = it }
            bestStreak?.let { updates["bestStreak"] = it }
            totalPoints?.let { updates["totalPoints"] = it }

            if (updates.isNotEmpty()) {
                firestore.collection("users").document(uid).update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getByGameId(gameId: String): User? {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("gameId", gameId)
                .limit(1)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}