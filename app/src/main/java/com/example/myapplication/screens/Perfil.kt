package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.data.models.User
import com.example.myapplication.auth.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.Logout
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapplication.auth.BiometricAuthResult
import com.example.myapplication.auth.BiometricAvailability

@Composable
fun Perfil(
    user: FirebaseUser?,
    userData: User?,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
    onEditProfile: () -> Unit,
    onVerAmigos: () -> Unit,
    onRefresh: () -> Unit
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

    val displayName = userData?.displayName ?: user?.displayName ?: "Usuario"
    val email = userData?.email ?: user?.email ?: "correo@ejemplo.com"
    val photoURL = userData?.photoURL ?: ""
    val userId = user?.uid ?: userData?.uid?.take(8) ?: ""

    // Datos reales de Firestore
    val totalPlaces = userData?.totalPlacesVisited ?: 0
    val currentStreak = userData?.currentStreak ?: 0
    val bestStreak = userData?.bestStreak ?: 0
    val totalPoints = userData?.totalPoints ?: 0
    val gameId = userData?.gameId ?: "linkup#0000"

    // Observar resultado de biometría para borrar cuenta
    LaunchedEffect(Unit) {
        biometricManager.authResult.collect { result ->
            when (result) {
                is BiometricAuthResult.Success -> {
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
                is BiometricAuthResult.Error -> {
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
        gameId = gameId,
        photoURL = photoURL,
        totalPlaces = totalPlaces,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        totalPoints = totalPoints,
        onEditClick = onEditProfile,
        onVerAmigosClick = onVerAmigos,
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
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón: Usar huella
                    if (biometricManager.isBiometricAvailable() is BiometricAvailability.Available) {
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
    gameId: String,
    photoURL: String,
    totalPlaces: Int,
    currentStreak: Int,
    bestStreak: Int,
    totalPoints: Int,
    onEditClick: () -> Unit,
    onVerAmigosClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accent = Color(0xFFFF9800)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header con foto y nombre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                if (photoURL.isNotBlank()) {
                    AsyncImage(
                        model = photoURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Imagen de perfil",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color(0xFFBDBDBD)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ID: $userId",
                        fontSize = 11.sp,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = gameId,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjetas de estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                value = totalPlaces.toString(),
                label = "Lugares",
                icon = Icons.Default.Place,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = currentStreak.toString(),
                label = "Racha actual",
                icon = Icons.Default.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = bestStreak.toString(),
                label = "Mejor racha",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = totalPoints.toString(),
                label = "XP",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menú de opciones
        MenuItem(
            icon = Icons.Default.Settings,
            title = "Configuración",
            subtitle = "Ajustes de la cuenta",
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

        MenuItem(
            icon = Icons.Default.PrivacyTip,
            title = "Privacidad de ubicación",
            subtitle = "¿Quién puede ver tu ubicación?",
            accent = accent,
            onClick = { /* Navegar a privacidad */ }
        )

        MenuItem(
            icon = Icons.Default.People,
            title = "Mis Amigos",
            subtitle = "Gestiona tus amigos",
            accent = accent,
            onClick = onVerAmigosClick
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
                    Icons.AutoMirrored.Filled.Logout,
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
                .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 24.dp)
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
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.Gray, fontSize = 10.sp)
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
