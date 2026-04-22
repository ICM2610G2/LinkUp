package com.example.myapplication.ui.theme.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedPreferences(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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