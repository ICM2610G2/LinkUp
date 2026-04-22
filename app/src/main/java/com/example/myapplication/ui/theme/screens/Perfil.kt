package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
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
import com.example.myapplication.ui.theme.auth.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun Perfil(
    user: FirebaseUser?,  // ← Recibir el usuario logueado
    onLogout: () -> Unit   // ← Callback cuando cierra sesión
) {
    val accent = Color(0xFFFF9800)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { FirebaseAuthManager(context as androidx.appcompat.app.AppCompatActivity) }

    // Obtener nombre y email del usuario
    val displayName = user?.displayName ?: "Usuario"
    val email = user?.email ?: "correo@ejemplo.com"
    val userId = user?.uid?.take(8) ?: ""

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
                    text = displayName,  // ← Nombre del usuario registrado
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,  // ← Email del usuario registrado
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
                .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 90.dp)
                .clickable {
                    scope.launch {
                        authManager.logout()
                        onLogout()  // Notificar que se cerró sesión
                    }
                },
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
                Text(
                    text = "Cerrar sesión",
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