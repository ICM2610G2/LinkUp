package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuFlotante(
    onCerrar: () -> Unit,
    onCrearCarrera: () -> Unit,
    onInvitarNFC: () -> Unit,
    onVerAmigos: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable { onCerrar() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).padding(bottom = 12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "¿Qué deseas hacer?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x1AFFFFFF), CircleShape)
                            .clickable {
                                Log.i("MyApp", "Cerrar menu flotante clicked")
                                onCerrar()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OpcionMenu(
                    titulo = "Crear carrera",
                    descripcion = "Configura una nueva ruta",
                    iconoColor = Color(0xFF2A9D8F),
                    fondoBoton = Color(0xFF2A9D8F),
                    onClick = {
                        Log.i("MyApp", "Crear carrera clicked desde menu flotante")
                        onCerrar()
                        onCrearCarrera()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OpcionMenu(
                    titulo = "Invitar por NFC",
                    descripcion = "Comparte con amigos cercanos",
                    iconoColor = Color(0xFF0A0A0A),
                    fondoBoton = Color(0xFFE9C46A),
                    textoColor = Color(0xFF0A0A0A),
                    descripcionColor = Color(0x99000000),
                    onClick = {
                        Log.i("MyApp", "Invitar NFC clicked desde menu flotante")
                        onCerrar()
                        onInvitarNFC()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OpcionMenu(
                    titulo = "Amigos",
                    descripcion = "Administra tu lista de amigos",
                    iconoColor = Color.White,
                    fondoBoton = Color(0xFF252525),
                    onClick = {
                        Log.i("MyApp", "Ver amigos clicked desde menu flotante")
                        onCerrar()
                        onVerAmigos()
                    }
                )
            }
        }
    }
}

@Composable
fun OpcionMenu(
    titulo: String,
    descripcion: String,
    iconoColor: Color,
    fondoBoton: Color,
    textoColor: Color = Color.White,
    descripcionColor: Color = Color.White.copy(alpha = 0.6f),
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = fondoBoton),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        iconoColor.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (titulo) {
                        "Crear carrera" -> Icons.Default.EmojiEvents
                        "Invitar por NFC" -> Icons.Default.Nfc
                        else -> Icons.Default.PersonAdd
                    },
                    contentDescription = null,
                    tint = iconoColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    color = textoColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    descripcion,
                    color = descripcionColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}