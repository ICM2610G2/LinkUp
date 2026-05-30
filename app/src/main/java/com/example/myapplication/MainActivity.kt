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
import com.example.myapplication.auth.EncryptedPreferences
import com.example.myapplication.auth.FirebaseAuthManager
import com.example.myapplication.auth.AuthState
import com.example.myapplication.navigation.MainScaffold
import com.example.myapplication.screens.Login
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
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
    val context = LocalContext.current
    val activity = context as AppCompatActivity
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val authManager = remember {
        _root_ide_package_.com.example.myapplication.auth.FirebaseAuthManager(
            activity
        )
    }
    val biometricManager = remember {
        _root_ide_package_.com.example.myapplication.auth.BiometricAuthManager(
            activity
        )
    }
    val encryptedPrefs = remember {
        _root_ide_package_.com.example.myapplication.auth.EncryptedPreferences(
            context
        )
    }

    val authState by authManager.authState.collectAsState()

    var userData by remember { mutableStateOf<User?>(null) }
    var isLoadingUserData by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        Log.d("LinkUpApp", "authState cambió: $authState")

        if (authState is AuthState.Authenticated) {
            isLoadingUserData = true
            val firebaseUser = (authState as AuthState.Authenticated).user
            Log.d("LinkUpApp", "Usuario autenticado: ${firebaseUser.uid}")

            try {
                // Intentar cargar desde Firestore
                var user = userRepository.getUser(firebaseUser.uid)
                Log.d("LinkUpApp", "Usuario encontrado en Firestore: ${user != null}")


                if (user == null) {
                    Log.d("LinkUpApp", " Usuario no existe en Firestore, creando...")
                    val gameId = authManager.generateUniqueGameId()
                    Log.d("LinkUpApp", "GameID generado: $gameId")

                    val saveResult = authManager.saveUserToFirestore(firebaseUser, gameId)

                    saveResult.fold(
                        onSuccess = {
                            Log.d("LinkUpApp", " Usuario creado en Firestore")
                            kotlinx.coroutines.delay(500)
                            user = userRepository.getUser(firebaseUser.uid)
                            Log.d("LinkUpApp", "Usuario recargado: ${user?.gameId}")
                        },
                        onFailure = { e ->
                            Log.e("LinkUpApp", " Error creando usuario: ${e.message}", e)
                        }
                    )
                }

                // ✅ Asegurar que el user tiene gameId incluso si existe pero está vacío
                if (user != null && user.gameId.isEmpty()) {
                    Log.d("LinkUpApp", "⚠ Usuario sin Game ID, actualizando...")
                    val newGameId = authManager.generateUniqueGameId()
                    val updatedUser = user.copy(gameId = newGameId)
                    userRepository.updateUser(updatedUser)
                    user = updatedUser
                }

                userData = user
                Log.d("LinkUpApp", " userData final: ${userData?.gameId}")

            } catch (e: Exception) {
                Log.e("LinkUpApp", "Error en LaunchedEffect: ${e.message}", e)
            }

            isLoadingUserData = false
        } else {
            userData = null
        }
    }

    when (authState) {
        is AuthState.Authenticated -> {
            if (isLoadingUserData) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            } else {
                MainScaffold(
                    user = userData,
                    onLogout = {
                        scope.launch { authManager.logout() }
                    },
                    onAccountDeleted = {
                        scope.launch { encryptedPrefs.clearUserCredentials() }
                    }
                )
            }
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
                onLoginSuccess = { /* authState cambia solo, LaunchedEffect se encarga */ },
                authManager = authManager,
                biometricManager = biometricManager,
                encryptedPrefs = encryptedPrefs
            )
        }
    }
}