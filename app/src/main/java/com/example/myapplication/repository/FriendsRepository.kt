package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.data.models.Friendship
import com.example.myapplication.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Obtener lista de amigos aceptados
    // Usa dos queries separadas (userA y userB) para evitar índice compuesto en Firestore
    suspend fun getAcceptedFriends(): List<User> {
        val userId = currentUserId ?: return emptyList()

        return try {
            // Solicitudes donde yo soy quien envió
            val asUserA = firestore.collection("friendships")
                .whereEqualTo("userA", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            // Solicitudes donde yo soy quien recibió
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

    // Obtener solicitudes pendientes recibidas
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

    // Enviar solicitud de amistad
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
                        // Puede que el otro me haya enviado solicitud primero — aceptar directamente
                        val userA = existing.getString("userA")
                        if (userA == toUserId) {
                            // El otro me envió solicitud, la acepto
                            acceptRequest(friendshipId)
                        } else {
                            Result.failure(Exception("Ya enviaste una solicitud a este usuario"))
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
                "requestedAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("friendships").document(friendshipId).set(friendship).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error sendFriendRequest: ${e.message}")
            Result.failure(e)
        }
    }

    // Aceptar solicitud
    suspend fun acceptRequest(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId)
                .update(
                    "status", "accepted",
                    "acceptedAt", com.google.firebase.Timestamp.now()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error acceptRequest: ${e.message}")
            Result.failure(e)
        }
    }

    // Rechazar/eliminar solicitud
    suspend fun rejectRequest(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error rejectRequest: ${e.message}")
            Result.failure(e)
        }
    }

    // Eliminar amigo (misma operación que rechazar — borra el documento)
    suspend fun removeFriend(friendUserId: String): Result<Unit> {
        val currentId = currentUserId
            ?: return Result.failure(Exception("No autenticado"))
        val friendshipId = Friendship.generateId(currentId, friendUserId)
        return rejectRequest(friendshipId)
    }

    // Bloquear usuario
    suspend fun blockUser(userId: String): Result<Unit> {
        val currentId = currentUserId
            ?: return Result.failure(Exception("No autenticado"))
        val friendshipId = Friendship.generateId(currentId, userId)

        return try {
            // Usamos set con merge para que funcione aunque no exista el documento
            firestore.collection("friendships").document(friendshipId)
                .set(
                    mapOf(
                        "userA" to currentId,
                        "userB" to userId,
                        "participants" to listOf(currentId, userId),
                        "status" to "blocked",
                        "requestedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error blockUser: ${e.message}")
            Result.failure(e)
        }
    }

    // Buscar usuario por Game ID (case-insensitive con toLowerCase)
    suspend fun searchUserByGameId(gameId: String): User? {
        return try {
            // Buscar tal como viene (el gameId siempre se guarda en minúsculas: "linkup#XXXX")
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("gameId", gameId.trim().lowercase())
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("FriendsRepo", "Error searchUserByGameId: ${e.message}")
            null
        }
    }

    // Obtener estado de amistad con otro usuario
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

    // Obtener solicitudes enviadas pendientes
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