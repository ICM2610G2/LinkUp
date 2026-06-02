package com.example.myapplication

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.data.models.User
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.auth.EncryptedPreferences
import com.example.myapplication.auth.FirebaseAuthManager
import com.example.myapplication.auth.AuthState
import com.example.myapplication.navigation.MainScaffold
import com.example.myapplication.screens.Login
import com.example.myapplication.utils.NFCManager
import com.example.myapplication.repository.FriendInviteRepository
import kotlinx.coroutines.launch

/**
 * MainActivity (ORQUESTADOR)
 * Responsable de la gestión del ciclo de vida NFC y recepción de datos mediante ReaderMode.
 */
class MainActivity : AppCompatActivity() {
    private val nfcManager = NFCManager()
    private lateinit var inviteRepository: FriendInviteRepository
    
    // Anti-spam debounce
    private var lastNfcTime: Long = 0
    private var lastUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inviteRepository = FriendInviteRepository()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinkUpApp(nfcManager)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // RECEPTOR REAL: ReaderMode activo para detectar tags NFC (incluyendo otros dispositivos si emularan).
        if (nfcManager.isAvailable(this) && nfcManager.isEnabled(this)) {
            nfcManager.enableReaderMode(this) { uid ->
                handleReceivedUid(uid)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Fallback para procesamiento vía Intent si el sistema no lo capturó por ReaderMode
        nfcManager.parseFromIntent(intent)?.let { uid ->
            handleReceivedUid(uid)
        }
    }

    private fun handleReceivedUid(uid: String) {
        val currentTime = System.currentTimeMillis()
        // Anti-spam: Ignorar mismo UID dentro de 3 segundos
        if (uid == lastUid && (currentTime - lastNfcTime) < 3000) {
            return
        }
        
        lastNfcTime = currentTime
        lastUid = uid

        Log.d("NFC_RECEIVE", "UID válido detectado por NFC: $uid")
        lifecycleScope.launch {
            val result = inviteRepository.sendInviteByUid(uid)
            runOnUiThread {
                result.fold(
                    onSuccess = { Toast.makeText(this@MainActivity, "¡Solicitud enviada por NFC!", Toast.LENGTH_SHORT).show() },
                    onFailure = { e -> Toast.makeText(this@MainActivity, "NFC: ${e.message}", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun LinkUpApp(nfcManager: NFCManager) {
    val context = LocalContext.current
    val activity = context as AppCompatActivity
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val authManager = remember { FirebaseAuthManager(activity) }
    val biometricManager = remember { BiometricAuthManager(activity) }
    val encryptedPrefs = remember { EncryptedPreferences(context) }

    val authState by authManager.authState.collectAsState()

    var userData by remember { mutableStateOf<User?>(null) }
    var isLoadingUserData by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            isLoadingUserData = true
            val firebaseUser = (authState as AuthState.Authenticated).user
            try {
                var user = userRepository.getUser(firebaseUser.uid)
                if (user == null) {
                    val gameId = authManager.generateUniqueGameId()
                    authManager.saveUserToFirestore(firebaseUser, gameId)
                    user = userRepository.getUser(firebaseUser.uid)
                }
                userData = user
            } catch (e: Exception) {
                Log.e("LinkUpApp", "Error cargando usuario: ${e.message}")
            }
            isLoadingUserData = false
        } else {
            userData = null
        }
    }

    when (authState) {
        is AuthState.Authenticated -> {
            if (isLoadingUserData) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            } else {
                MainScaffold(
                    user = userData,
                    onLogout = { scope.launch { authManager.logout() } },
                    onAccountDeleted = { scope.launch { encryptedPrefs.clearUserCredentials() } }
                )
            }
        }
        AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Verificando sesión...")
            }
        }
        else -> {
            Login(
                onLoginSuccess = { },
                authManager = authManager,
                biometricManager = biometricManager,
                encryptedPrefs = encryptedPrefs
            )
        }
    }
}
