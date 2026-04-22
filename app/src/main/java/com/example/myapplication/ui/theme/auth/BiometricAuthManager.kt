package com.example.myapplication.auth

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricAuthManager(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)
    private val _authResult = Channel<BiometricAuthResult>()
    val authResult = _authResult.receiveAsFlow()

    private lateinit var executor: java.util.concurrent.Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun isBiometricAvailable(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("HUELLA_BORRAR", "Biometría disponible")
                BiometricAvailability.Available
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("HUELLA_BORRAR", "No hay sensor de huellas")
                BiometricAvailability.NotAvailable("No hay sensor de huellas")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("HUELLA_BORRAR", "Sensor no disponible")
                BiometricAvailability.NotAvailable("Sensor no disponible")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("HUELLA_BORRAR", "No hay huellas registradas")
                BiometricAvailability.NotAvailable("No hay huellas registradas")
            }
            else -> {
                Log.d("HUELLA_BORRAR", "Error desconocido")
                BiometricAvailability.NotAvailable("Error desconocido")
            }
        }
    }

    fun setupBiometricPrompt() {
        Log.d("HUELLA_BORRAR", "setupBiometricPrompt llamado")
        executor = ContextCompat.getMainExecutor(activity)

        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d("HUELLA_BORRAR", "Error autenticación: $errString")
                    _authResult.trySend(BiometricAuthResult.Error(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("HUELLA_BORRAR", "Autenticación exitosa")
                    _authResult.trySend(BiometricAuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d("HUELLA_BORRAR", "Autenticación fallida")
                    _authResult.trySend(BiometricAuthResult.Failed)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar eliminación de cuenta")
            .setSubtitle("Esta acción es irreversible")
            .setDescription("Coloca tu dedo en el sensor para confirmar que quieres eliminar tu cuenta")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    fun authenticate() {
        Log.d("HUELLA_BORRAR", "authenticate llamado")
        if (isBiometricAvailable() is BiometricAvailability.Available) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            _authResult.trySend(BiometricAuthResult.Error("Biometría no disponible"))
        }
    }
}

sealed class BiometricAvailability {
    object Available : BiometricAvailability()
    data class NotAvailable(val reason: String) : BiometricAvailability()
}

sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object Failed : BiometricAuthResult()
    data class Error(val message: String) : BiometricAuthResult()
}