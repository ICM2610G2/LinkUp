package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.ui.theme.auth.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun Perfil(
    user: FirebaseUser?,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as androidx.appcompat.app.AppCompatActivity
    val authManager = remember { FirebaseAuthManager(activity) }
    val biometricManager = remember { BiometricAuthManager(activity) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBiometricConfirm by remember { mutableStateOf(false) }

    val displayName = user?.displayName ?: "Usuario"
    val email = user?.email ?: "correo@ejemplo.com"
    val userId = user?.uid?.take(8) ?: ""

    // Observar resultado de biometría para borrar cuenta
    LaunchedEffect(Unit) {
        biometricManager.authResult.collect { result ->
            when (result) {
                is com.example.myapplication.auth.BiometricAuthResult.Success -> {
                    scope.launch {
                        isLoading = true
                        val deleteResult = authManager.deleteAccount()
                        deleteResult.fold(
                            onSuccess = {
                                onAccountDeleted()
                            },
                            onFailure = { e ->
                                errorMessage = "Error al borrar cuenta: ${e.message}"
                                isLoading = false
                                showBiometricConfirm = false
                            }
                        )
                    }
                }
                is com.example.myapplication.auth.BiometricAuthResult.Error -> {
                    errorMessage = result.message
                    showBiometricConfirm = false
                }
                else -> {}
            }
        }
    }

    PerfilContent(
        displayName = displayName,
        email = email,
        userId = userId,
        onLogoutClick = {
            scope.launch {
                authManager.logout()
                onLogout()
            }
        },
        onDeleteClick = { showDeleteDialog = true }
    )

    // Diálogo de confirmación para borrar cuenta
    if (showDeleteDialog) {
        Dialog(
            onDismissRequest = { showDeleteDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Borrar cuenta?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta acción es irreversible. Se eliminarán todos tus datos.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón: Usar huella
                    if (biometricManager.isBiometricAvailable() is com.example.myapplication.auth.BiometricAvailability.Available) {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                showBiometricConfirm = true
                                biometricManager.setupBiometricPrompt()
                                biometricManager.authenticate()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar con huella")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Botón: Usar contraseña
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            showPasswordDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar con contraseña")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            }
        }
    }

    // Diálogo para ingresar contraseña
    if (showPasswordDialog) {
        Dialog(
            onDismissRequest = {
                showPasswordDialog = false
                deletePassword = ""
                errorMessage = null
            }
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Confirmar con contraseña",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // CORREGIDO: TextField simplificado
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val result = authManager.deleteAccountWithPassword(deletePassword)
                                result.fold(
                                    onSuccess = {
                                        onAccountDeleted()
                                    },
                                    onFailure = { e ->
                                        errorMessage = e.message
                                        isLoading = false
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Confirmar y borrar cuenta")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            showPasswordDialog = false
                            deletePassword = ""
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilContent(
    displayName: String,
    email: String,
    userId: String,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accent = Color(0xFFFF9800)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(accent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Imagen de perfil",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color(0xFFBDBDBD)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ID: $userId",
                    fontSize = 14.sp,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MenuItem(
            icon = Icons.Default.Settings,
            title = "Configuración",
            accent = accent,
            onClick = { /* Navegar a configuración */ }
        )

        MenuItem(
            icon = Icons.Default.Timeline,
            title = "Historial de carreras",
            subtitle = "Ver todas tus carreras",
            accent = accent,
            onClick = { /* Navegar a historial */ }
        )

        MenuItem(
            icon = Icons.Default.CameraAlt,
            title = "Fotos guardadas",
            subtitle = "Revisa todos tus recuerdos",
            accent = accent,
            onClick = { /* Navegar a fotos */ }
        )

        MenuItem(
            icon = Icons.Default.LocationOn,
            title = "Sensores del dispositivo",
            subtitle = "Acelerómetro, GPS, Magnetómetro",
            accent = accent,
            onClick = { /* Navegar a sensores */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botón de cerrar sesión
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 8.dp)
                .clickable { onLogoutClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar sesión",
                    color = Color(0xFFFF6B6B),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botón de borrar cuenta
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 90.dp)
                .clickable { onDeleteClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0A0A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Borrar cuenta",
                    color = Color(0xFFFF6B6B),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    accent: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape)
                    .border(1.dp, accent.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "›",
                color = Color(0xFF7A7A7A),
                fontSize = 22.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    MyApplicationTheme {
        PerfilContent(
            displayName = "Juan Pérez",
            email = "juan.perez@example.com",
            userId = "ABC12345",
            onLogoutClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B0B)
@Composable
fun MenuItemPreview() {
    MyApplicationTheme {
        MenuItem(
            icon = Icons.Default.Settings,
            title = "Configuración",
            subtitle = "Ajustes de la cuenta",
            accent = Color(0xFFFF9800),
            onClick = {}
        )
    }
}
