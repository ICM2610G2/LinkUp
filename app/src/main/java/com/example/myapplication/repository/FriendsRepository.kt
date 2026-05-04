package com.example.myapplication.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.example.myapplication.data.models.User

data class Friendship(
    val id: String = "",
    val userA: String = "",
    val userB: String = "",
    val status: String = "pending", // pending, accepted, blocked
    val requestedAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val acceptedAt: com.google.firebase.Timestamp? = null
)

class FriendsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getFriends(uid: String): Flow<List<User>> = flow {
        val querySnapshot = firestore.collection("friendships")
            .whereEqualTo("status", "accepted")
            .whereArrayContains("participants", uid)
            .get()
            .await()

        val friendIds = querySnapshot.documents.flatMap { doc ->
            val participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            participants.filter { it != uid }
        }

        val friends = friendIds.mapNotNull { friendId ->
            firestore.collection("users").document(friendId).get().await().toObject(User::class.java)
        }

        emit(friends)
    }

    suspend fun sendFriendRequest(fromUid: String, toUid: String): Result<Unit> {
        return try {
            val docId = if (fromUid < toUid) "${fromUid}_${toUid}" else "${toUid}_${fromUid}"
            val friendship = hashMapOf(
                "userA" to fromUid,
                "userB" to toUid,
                "participants" to listOf(fromUid, toUid),
                "status" to "pending",
                "requestedAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("friendships").document(docId).set(friendship).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId)
                .update("status", "accepted", "acceptedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}