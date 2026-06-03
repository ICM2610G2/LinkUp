package com.example.myapplication.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedPreferences(private val context: Context) {

    companion object {
        private const val TAG = "EncryptedPreferences"
        private const val PREFS_FILE = "secure_prefs"
    }

    private val sharedPreferences = createEncryptedPrefs()

    /**
     * Intenta crear EncryptedSharedPreferences normalmente.
     * Si la llave del Keystore está inválida (reinstalación, cambio de PIN/huella,
     * o diferencia de versión Android), borra las prefs corruptas y las recrea limpias.
     * El usuario tendrá que volver a iniciar sesión, pero la app no crashea.
     */
    private fun createEncryptedPrefs(): EncryptedSharedPreferences {
        return try {
            buildPrefs()
        } catch (e: Exception) {
            Log.w(TAG, "Prefs corruptas o llave inválida — limpiando y recreando: ${e.message}")
            try {
                context.deleteSharedPreferences(PREFS_FILE)
                buildPrefs()
            } catch (e2: Exception) {
                // Segundo fallo: algo más grave con el Keystore del dispositivo
                Log.e(TAG, "No se pudo recrear EncryptedSharedPreferences: ${e2.message}")
                throw e2
            }
        }
    }

    private fun buildPrefs(): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    suspend fun saveUserCredentials(email: String, password: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                putString("user_email", email)
                putString("user_password", password)
                putBoolean("biometric_enabled", true)
                apply()
            }
        }
    }

    suspend fun getUserCredentials(): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            val email = sharedPreferences.getString("user_email", null)
            val password = sharedPreferences.getString("user_password", null)
            Pair(email, password)
        }
    }

    suspend fun isBiometricEnabled(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean("biometric_enabled", false)
        }
    }

    suspend fun clearUserCredentials() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
        }
    }
}