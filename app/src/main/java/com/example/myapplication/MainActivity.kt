package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.auth.EncryptedPreferences
import com.example.myapplication.ui.theme.auth.FirebaseAuthManager
import com.example.myapplication.ui.theme.auth.AuthState
import com.example.myapplication.ui.theme.navigation.MainScaffold
import com.example.myapplication.ui.theme.screens.Login
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {  // ← CAMBIADO: AppCompatActivity en lugar de ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate llamado")

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
    Log.d("LinkUpApp", "LinkUpApp composable iniciado")

    val context = LocalContext.current
    val activity = context as AppCompatActivity  // ← CAMBIADO: AppCompatActivity
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    val authManager = remember { FirebaseAuthManager(activity) }
    val biometricManager = remember { BiometricAuthManager(activity) }
    val encryptedPrefs = remember { EncryptedPreferences(context) }

    val authState by authManager.authState.collectAsState()

    Log.d("LinkUpApp", "authState: $authState")

    // Estado para userData desde Firestore
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoadingUserData by remember { mutableStateOf(false) }

    // Cargar userData cuando haya un usuario autenticado
    LaunchedEffect(authState) {
        Log.d("LinkUpApp", "LaunchedEffect ejecutado con authState: $authState")
        if (authState is AuthState.Authenticated) {
            Log.d("LinkUpApp", "Usuario autenticado, cargando userData...")
            isLoadingUserData = true
            val firebaseUser = (authState as AuthState.Authenticated).user
            Log.d("LinkUpApp", "FirebaseUser UID: ${firebaseUser.uid}")
            try {
                userData = userRepository.getUser(firebaseUser.uid)
                Log.d("LinkUpApp", "userData cargado: $userData")
            } catch (e: Exception) {
                Log.e("LinkUpApp", "Error cargando userData: ${e.message}", e)
            }
            isLoadingUserData = false
        } else {
            Log.d("LinkUpApp", "Usuario no autenticado, userData = null")
            userData = null
        }
    }

    when (val state = authState) {
        is AuthState.Authenticated -> {
            Log.d("LinkUpApp", "Estado: Authenticated, isLoadingUserData=$isLoadingUserData")
            if (isLoadingUserData) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            } else {
                Log.d("LinkUpApp", "Mostrando MainScaffold con userData=$userData")
                MainScaffold(
                    user = userData,
                    onLogout = {
                        Log.d("LinkUpApp", "onLogout llamado")
                        scope.launch {
                            authManager.logout()
                        }
                    },
                    onAccountDeleted = {
                        Log.d("LinkUpApp", "onAccountDeleted llamado")
                        scope.launch {
                            encryptedPrefs.clearUserCredentials()
                        }
                    }
                )
            }
        }
        AuthState.Loading -> {
            Log.d("LinkUpApp", "Estado: Loading")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Verificando sesión...")
            }
        }
        else -> {
            Log.d("LinkUpApp", "Estado: No autenticado (Login)")
            Login(
                onLoginSuccess = {
                    Log.d("LinkUpApp", "Login exitoso")
                },
                authManager = authManager,
                biometricManager = biometricManager,
                encryptedPrefs = encryptedPrefs
            )
        }
    }
}