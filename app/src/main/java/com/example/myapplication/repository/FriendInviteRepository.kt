package com.example.myapplication.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendInviteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val friendsRepository: FriendsRepository = FriendsRepository(firestore, auth)
) {
    private val TAG = "FriendInviteRepository"

    suspend fun sendInviteByUid(uid: String): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
        
        if (currentUser.uid == uid) {
            return Result.failure(Exception("No puedes enviarte una invitación a ti mismo"))
        }

        return try {
            Log.d(TAG, "Verificando existencia del usuario UID: $uid")
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                Log.e(TAG, "El usuario $uid no existe en Firestore")
                return Result.failure(Exception("El usuario no existe"))
            }
            
            Log.d(TAG, "Usuario validado. Ejecutando friendsRepository.sendFriendRequest($uid)")
            val result = friendsRepository.sendFriendRequest(uid)
            
            result.onSuccess {
                Log.d(TAG, "Solicitud de amistad enviada exitosamente vía FriendsRepository")
            }.onFailure { e ->
                Log.e(TAG, "Error en FriendsRepository al enviar solicitud: ${e.message}")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Excepción crítica en sendInviteByUid: ${e.message}", e)
            Result.failure(e)
        }
    }
}
