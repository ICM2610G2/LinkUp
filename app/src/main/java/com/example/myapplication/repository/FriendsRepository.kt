package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.data.models.Friendship
import com.example.myapplication.data.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.util.*

class FriendsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getAcceptedFriends(): List<User> {
        val userId = currentUserId ?: return emptyList()

        return try {
            val asUserA = firestore.collection("friendships")
                .whereEqualTo("userA", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            val asUserB = firestore.collection("friendships")
                .whereEqualTo("userB", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            val friendIds = mutableSetOf<String>()

            asUserA.documents.forEach { doc ->
                doc.getString("userB")?.let { friendIds.add(it) }
            }
            asUserB.documents.forEach { doc ->
                doc.getString("userA")?.let { friendIds.add(it) }
            }

            if (friendIds.isEmpty()) return emptyList()

            val friends = mutableListOf<User>()
            for (friendId in friendIds) {
                val userDoc = firestore.collection("users").document(friendId).get().await()
                userDoc.toObject(User::class.java)?.let { friends.add(it) }
            }
            friends
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error getAcceptedFriends: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPendingRequests(): List<Friendship> {
        val userId = currentUserId ?: return emptyList()

        return try {
            val querySnapshot = firestore.collection("friendships")
                .whereEqualTo("userB", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error getPendingRequests: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendFriendRequest(toUserId: String): Result<Unit> {
        val fromUserId = currentUserId
            ?: return Result.failure(Exception("Usuario no autenticado"))

        if (fromUserId == toUserId)
            return Result.failure(Exception("No puedes enviarte una solicitud a ti mismo"))

        return try {
            val friendshipId = Friendship.generateId(fromUserId, toUserId)
            val existing = firestore.collection("friendships").document(friendshipId).get().await()

            if (existing.exists()) {
                return when (existing.getString("status")) {
                    "pending" -> {
                        val userA = existing.getString("userA")
                        if (userA == toUserId) {
                            acceptRequest(friendshipId)
                        } else {
                            Result.failure(Exception("Ya existe una solicitud pendiente"))
                        }
                    }
                    "accepted" -> Result.failure(Exception("Ya son amigos"))
                    "blocked" -> Result.failure(Exception("No puedes enviar solicitud a este usuario"))
                    else -> Result.failure(Exception("Ya existe una relación con este usuario"))
                }
            }

            val friendship = hashMapOf(
                "userA" to fromUserId,
                "userB" to toUserId,
                "participants" to listOf(fromUserId, toUserId),
                "status" to "pending",
                "requestedAt" to Timestamp.now()
            )

            firestore.collection("friendships").document(friendshipId).set(friendship).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error sendFriendRequest: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun acceptRequest(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId)
                .update(
                    "status", "accepted",
                    "acceptedAt", Timestamp.now()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error acceptRequest: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun rejectRequest(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error rejectRequest: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendUserId: String): Result<Unit> {
        val currentId = currentUserId
            ?: return Result.failure(Exception("No autenticado"))
        val friendshipId = Friendship.generateId(currentId, friendUserId)
        return rejectRequest(friendshipId)
    }

    suspend fun blockUser(userId: String): Result<Unit> {
        val currentId = currentUserId
            ?: return Result.failure(Exception("No autenticado"))
        val friendshipId = Friendship.generateId(currentId, userId)

        return try {
            firestore.collection("friendships").document(friendshipId)
                .set(
                    mapOf(
                        "userA" to currentId,
                        "userB" to userId,
                        "participants" to listOf(currentId, userId),
                        "status" to "blocked",
                        "requestedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error blockUser: ${e.message}")
            Result.failure(e)
        }
    }

    // --- NUEVA LÓGICA DE CÓDIGOS TEMPORALES ---

    suspend fun generateFriendInvite(): String {
        val userId = currentUserId ?: throw Exception("No autenticado")
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var code = ""
        var isUnique = false

        while (!isUnique) {
            code = (1..8).map { chars.random() }.joinToString("")
            val existing = firestore.collection("friend_invites")
                .whereEqualTo("code", code)
                .get()
                .await()
            if (existing.isEmpty) isUnique = true
        }

        val now = Calendar.getInstance()
        val createdAt = Timestamp(now.time)
        now.add(Calendar.MINUTE, 15)
        val expiresAt = Timestamp(now.time)

        val invite = hashMapOf(
            "code" to code,
            "ownerUid" to userId,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt
        )

        firestore.collection("friend_invites").document(code).set(invite).await()
        return code
    }

    suspend fun getUserByInviteCode(code: String): Result<User> {
        return try {
            val doc = firestore.collection("friend_invites").document(code.uppercase()).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Código inválido"))
            }

            val expiresAt = doc.getTimestamp("expiresAt")
            if (expiresAt != null && expiresAt.toDate().before(Date())) {
                return Result.failure(Exception("El código ha expirado"))
            }

            val ownerUid = doc.getString("ownerUid") ?: return Result.failure(Exception("Error en el código"))
            
            val userDoc = firestore.collection("users").document(ownerUid).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendshipStatus(otherUserId: String): String? {
        val currentId = currentUserId ?: return null
        val friendshipId = Friendship.generateId(currentId, otherUserId)

        return try {
            val doc = firestore.collection("friendships").document(friendshipId).get().await()
            if (doc.exists()) doc.getString("status") else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSentRequests(): List<Friendship> {
        val userId = currentUserId ?: return emptyList()

        return try {
            firestore.collection("friendships")
                .whereEqualTo("userA", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
                .documents
                .mapNotNull { doc -> doc.toObject(Friendship::class.java)?.copy(id = doc.id) }
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error getSentRequests: ${e.message}")
            emptyList()
        }
    }
}