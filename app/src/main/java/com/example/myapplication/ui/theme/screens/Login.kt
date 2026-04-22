package com.example.myapplication.ui.theme.screens

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R
import com.example.myapplication.ui.theme.auth.*
import kotlinx.coroutines.launch
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.auth.BiometricAuthResult
import com.example.myapplication.auth.BiometricAvailability


@Composable
fun Login(
    onLoginSuccess: () -> Unit,
    authManager: FirebaseAuthManager,
    biometricManager: BiometricAuthManager,
    encryptedPrefs: EncryptedPreferences
) {
    val context = LocalContext.current
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
        Log.d("HUELLA", "=== INICIANDO VERIFICACIÓN DE HUELLA ===")
        val isBiometricEnabled = encryptedPrefs.isBiometricEnabled()
        Log.d("HUELLA", "isBiometricEnabled: $isBiometricEnabled")

        if (isBiometricEnabled) {
            val (savedEmail, savedPassword) = encryptedPrefs.getUserCredentials()
            Log.d("HUELLA", "savedEmail: $savedEmail")
            Log.d("HUELLA", "savedPassword existe: ${savedPassword != null}")

            if (savedEmail != null && savedPassword != null) {
                email = savedEmail
                password = savedPassword
                Log.d("HUELLA", "Credenciales cargadas, preparando biometría...")

                val available = biometricManager.isBiometricAvailable()
                Log.d("HUELLA", "Biometría disponible: $available")

                biometricManager.setupBiometricPrompt()
                Log.d("HUELLA", "BiometricPrompt configurado")
                biometricManager.authenticate()
                Log.d("HUELLA", "authenticate() llamado")
            } else {
                Log.d("HUELLA", "No hay credenciales guardadas (email o password null)")
            }
        } else {
            Log.d("HUELLA", "Biometría NO habilitada en preferencias")
        }
    }

    // Launcher para Google Sign-In
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            isLoading = true
            val authResult = authManager.handleGoogleSignInResult(result.data)
            authResult.fold(
                onSuccess = {
                    Log.d("HUELLA", "Google login exitoso")
                    onLoginSuccess()
                },
                onFailure = {
                    errorMessage = "Error con Google: ${it.message}"
                    isLoading = false
                }
            )
        }
    }

    // Observar resultado de biometría
    LaunchedEffect(Unit) {
        biometricManager.authResult.collect { result ->
            Log.d("HUELLA", "Resultado de biometría recibido: $result")
            when (result) {
                is BiometricAuthResult.Success -> {
                    Log.d("HUELLA", "Biometría exitosa, iniciando sesión...")
                    scope.launch {
                        isLoading = true
                        val (savedEmail, savedPassword) = encryptedPrefs.getUserCredentials()
                        if (savedEmail != null && savedPassword != null) {
                            val loginResult = authManager.loginWithEmail(savedEmail, savedPassword)
                            loginResult.fold(
                                onSuccess = {
                                    Log.d("HUELLA", "Login con huella exitoso")
                                    onLoginSuccess()
                                },
                                onFailure = {
                                    errorMessage = "Error al iniciar con huella: ${it.message}"
                                    isLoading = false
                                }
                            )
                        }
                    }
                }
                is BiometricAuthResult.Error -> {
                    Log.d("HUELLA", "Error de biometría: ${result.message}")
                    errorMessage = result.message
                }
                is BiometricAuthResult.Failed -> {
                    Log.d("HUELLA", "Biometría fallida")
                }
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
                        listOf(
                            Color(0xB3000000),
                            Color(0x80000000),
                            Color(0xB3000000)
                        )
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

                        val result = if (esRegistro) {
                            if (name.isBlank()) {
                                errorMessage = "Ingresa tu nombre"
                                isLoading = false
                                return@launch
                            }
                            authManager.registerWithEmail(email, password, name)
                        } else {
                            authManager.loginWithEmail(email, password)
                        }

                        result.fold(
                            onSuccess = { user ->
                                Log.d("HUELLA", "${if (esRegistro) "Registro" else "Login"} exitoso para: ${user.email}")
                                // Guardar credenciales SIEMPRE (tanto en registro como en login)
                                Log.d("HUELLA", "Guardando credenciales para: $email")
                                encryptedPrefs.saveUserCredentials(email, password)
                                Log.d("HUELLA", "Credenciales guardadas")
                                onLoginSuccess()
                            },
                            onFailure = { exception ->
                                errorMessage = when {
                                    exception.message?.contains("email") == true -> "Email inválido"
                                    exception.message?.contains("password") == true -> "Contraseña débil (mínimo 6 caracteres)"
                                    exception.message?.contains("already in use") == true -> "Email ya registrado"
                                    exception.message?.contains("wrong password") == true -> "Contraseña incorrecta"
                                    exception.message?.contains("user-not-found") == true -> "Usuario no encontrado"
                                    else -> "Error: ${exception.message}"
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
                onGoogle = {
                    scope.launch {
                        isLoading = true
                        val signInIntent = authManager.getGoogleSignInIntent()
                        googleLauncher.launch(signInIntent)
                    }
                },
                onBiometric = {
                    Log.d("HUELLA", "Botón de huella presionado manualmente")
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
            Text(
                "Link",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Up",
                color = Color(0xFFFF9800),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
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
    onGoogle: () -> Unit,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        if (esRegistro) "Crear cuenta" else "Iniciar sesión",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
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

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x1AFFFFFF))
            )

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}