package com.example.myapplication.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendInviteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val friendsRepository: FriendsRepository = FriendsRepository(firestore, auth)
) {
    suspend fun sendInviteByUid(uid: String): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
        
        if (currentUser.uid == uid) {
            return Result.failure(Exception("No puedes enviarte una invitación a ti mismo"))
        }

        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("El usuario no existe"))
            }
            
            friendsRepository.sendFriendRequest(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
