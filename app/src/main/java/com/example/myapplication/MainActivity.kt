package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.ui.theme.auth.EncryptedPreferences
import com.example.myapplication.ui.theme.auth.FirebaseAuthManager
import com.example.myapplication.ui.theme.auth.AuthState
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.navigation.MainScaffold
import com.example.myapplication.ui.theme.screens.Login

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinkUpApp()
                }
            }
        }
    }
}

@Composable
fun LinkUpApp() {
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    val authManager = remember { FirebaseAuthManager(activity) }
    val biometricManager = remember { BiometricAuthManager(activity.supportFragmentManager) }
    val encryptedPrefs = remember { EncryptedPreferences(context) }

    val authState by authManager.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Authenticated -> {
            // Usuario autenticado - mostrar MainScaffold con sus datos
            MainScaffold(
                user = state.user,  // ← Pasar el usuario autenticado
                onLogout = {
                    // El logout ya maneja el cambio de estado automáticamente
                }
            )
        }
        AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Verificando sesión...")
            }
        }
        else -> {
            Login(
                onLoginSuccess = {},
                authManager = authManager,
                biometricManager = biometricManager,
                encryptedPrefs = encryptedPrefs
            )
        }
    }
}