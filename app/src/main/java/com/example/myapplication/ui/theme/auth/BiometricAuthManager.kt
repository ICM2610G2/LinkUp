package com.example.myapplication.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricAuthManager(private val fragmentManager: FragmentManager) {

    private val _authResult = Channel<BiometricAuthResult>()
    val authResult = _authResult.receiveAsFlow()

    private lateinit var executor: java.util.concurrent.Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun isBiometricAvailable(): BiometricAvailability {
        // Necesitamos un contexto para esto, lo obtenemos del fragmentManager
        val context = fragmentManager.findFragmentByTag("any")?.context ?: return BiometricAvailability.NotAvailable("Contexto no disponible")
        val biometricManager = BiometricManager.from(context)

        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NotAvailable("No hay sensor de huellas")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.NotAvailable("Sensor no disponible")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotAvailable("No hay huellas registradas")
            else -> BiometricAvailability.NotAvailable("Error desconocido")
        }
    }

    fun setupBiometricPrompt() {
        val context = fragmentManager.findFragmentByTag("any")?.context ?: return
        executor = ContextCompat.getMainExecutor(context)

        biometricPrompt = BiometricPrompt(
            fragmentManager.findFragmentByTag("any") as? androidx.fragment.app.FragmentActivity ?: return,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    _authResult.trySend(BiometricAuthResult.Error(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _authResult.trySend(BiometricAuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _authResult.trySend(BiometricAuthResult.Failed)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con huella")
            .setSubtitle("Verifica tu identidad para continuar")
            .setDescription("Coloca tu dedo en el sensor")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    fun authenticate() {
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