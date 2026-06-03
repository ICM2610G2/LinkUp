package com.example.myapplication.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Repositorio responsable de operaciones con Firebase Storage.
 * Sube archivos y devuelve URLs públicas de descarga.
 */
class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Sube la imagen de perfil a Storage y devuelve la URL pública de descarga.
     * Al usar el uid como nombre fijo, cada subida sobreescribe la anterior
     * Devuelve la URL pública de descarga que se guarda en Firestore.
     */
    suspend fun uploadProfilePicture(imageUri: Uri): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Referencia fija por UID — sobreescribe siempre la misma imagen
            val ref = storage.reference.child("profile_pictures/$uid.jpg")

            // Sube el archivo y espera confirmación
            ref.putFile(imageUri).await()

            // Obtiene la URL pública de descarga (permanente, no expira)
            val downloadUrl = ref.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}