package com.example.myapplication.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.auth.*
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.auth.BiometricAuthResult
import com.example.myapplication.auth.BiometricAvailability
import kotlinx.coroutines.launch

@Composable
fun Login(
    onLoginSuccess: () -> Unit,
    authManager: FirebaseAuthManager,
    biometricManager: BiometricAuthManager,
    encryptedPrefs: EncryptedPreferences
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var esRegistro by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }

    // Verificar credenciales guardadas para huella
    LaunchedEffect(Unit) {
        val isBiometricEnabled = encryptedPrefs.isBiometricEnabled()
        if (isBiometricEnabled) {
            val (savedEmail, savedPassword) = encryptedPrefs.getUserCredentials()
            if (savedEmail != null && savedPassword != null) {
                email = savedEmail
                password = savedPassword
                biometricManager.setupBiometricPrompt()
                biometricManager.authenticate()
            }
        }
    }

    // Observar resultado de biometría
    LaunchedEffect(Unit) {
        biometricManager.authResult.collect { result ->
            when (result) {
                is BiometricAuthResult.Success -> {
                    isLoading = true
                    val (savedEmail, savedPassword) = encryptedPrefs.getUserCredentials()
                    if (savedEmail != null && savedPassword != null) {
                        // Login con email/password — Firestore lo maneja MainActivity
                        authManager.loginWithEmail(savedEmail, savedPassword).fold(
                            onSuccess = { isLoading = false },  // authState cambia solo
                            onFailure = { e ->
                                errorMessage = "Error al iniciar con huella: ${e.message}"
                                isLoading = false
                            }
                        )
                    }
                }
                is BiometricAuthResult.Error -> errorMessage = result.message
                is BiometricAuthResult.Failed -> { /* no-op */ }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bogota_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xB3000000), Color(0x80000000), Color(0xB3000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoSection()
            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCCFF0000)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            FormularioLogin(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibleChange = { passwordVisible = it },
                esRegistro = esRegistro,
                name = name,
                onNameChange = { name = it },
                isLoading = isLoading,
                onLogin = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Ingresa email y contraseña"
                            isLoading = false
                            return@launch
                        }
                        if (esRegistro && name.isBlank()) {
                            errorMessage = "Ingresa tu nombre"
                            isLoading = false
                            return@launch
                        }

                        val result = if (esRegistro) {
                            authManager.registerWithEmail(email, password, name)
                        } else {
                            authManager.loginWithEmail(email, password)
                        }

                        result.fold(
                            onSuccess = {
                                // ✅ NO tocar Firestore aquí
                                // MainActivity.LaunchedEffect detecta el cambio de authState
                                // y se encarga de crear/cargar el usuario en Firestore
                                encryptedPrefs.saveUserCredentials(email, password)
                                // isLoading no hace falta en false: este composable
                                // se destruye cuando authState cambia a Authenticated
                            },
                            onFailure = { e ->
                                Log.e("AUTH", "Error: ${e.message}", e)
                                errorMessage = when {
                                    e.message?.contains("already in use", ignoreCase = true) == true ->
                                        "Email ya registrado"
                                    e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                                        "Email o contraseña incorrectos"
                                    e.message?.contains("wrong-password", ignoreCase = true) == true ->
                                        "Contraseña incorrecta"
                                    e.message?.contains("user-not-found", ignoreCase = true) == true ->
                                        "Usuario no encontrado"
                                    e.message?.contains("password", ignoreCase = true) == true ->
                                        "La contraseña debe tener al menos 6 caracteres"
                                    e.message?.contains("network", ignoreCase = true) == true ->
                                        "Error de red. Verifica tu conexión"
                                    else -> "Error: ${e.message}"
                                }
                                isLoading = false
                            }
                        )
                    }
                },
                onToggleRegistro = {
                    esRegistro = !esRegistro
                    errorMessage = null
                    name = ""
                },
                onBiometric = {
                    biometricManager.setupBiometricPrompt()
                    biometricManager.authenticate()
                },
                isBiometricAvailable = biometricManager.isBiometricAvailable() is BiometricAvailability.Available
            )
        }
    }
}

@Composable
fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Text("Link", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Text("Up", color = Color(0xFFFF9800), fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Explora tu ciudad con amigos",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun FormularioLogin(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    esRegistro: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onToggleRegistro: () -> Unit,
    onBiometric: () -> Unit,
    isBiometricAvailable: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x66000000)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            if (esRegistro) {
                Text("Nombre", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    placeholder = { Text("Tu nombre", color = Color.White.copy(alpha = 0.4f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x1AFFFFFF),
                        unfocusedContainerColor = Color(0x1AFFFFFF),
                        cursorColor = Color(0xFFFF9800),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Email", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                placeholder = { Text("tu@email.com", color = Color.White.copy(alpha = 0.4f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x1AFFFFFF),
                    cursorColor = Color(0xFFFF9800),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Contraseña", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                placeholder = { Text("••••••••", color = Color.White.copy(alpha = 0.4f)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x1AFFFFFF),
                    cursorColor = Color(0xFFFF9800),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (esRegistro) "Crear cuenta" else "Iniciar sesión",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            if (isBiometricAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBiometric,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar con huella digital", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onToggleRegistro) {
                    Text(
                        if (esRegistro) "¿Ya tienes cuenta? Inicia sesión"
                        else "¿No tienes cuenta? Regístrate",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}