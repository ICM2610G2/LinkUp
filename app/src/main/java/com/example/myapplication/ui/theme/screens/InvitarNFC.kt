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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun InvitarNFC(onCerrar: () -> Unit) {

    var mostrarQR by remember { mutableStateOf(false) }
    var escaneando by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                HeaderInvitar(onCerrar = {
                    Log.i("MyApp", "Cerrar invitar NFC clicked")
                    onCerrar()
                })

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BotonNFC(
                        escaneando = escaneando,
                        onClick = {
                            escaneando = true
                            Log.i("MyApp", "NFC scan iniciado")
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                escaneando = false
                                Log.i("MyApp", "NFC scan completado")
                            }, 2000)
                        }
                    )

                    SeparadorO()

                    BotonOpcion(
                        emoji = null,
                        iconoColor = Color(0xFFE9C46A),
                        fondoIcono = Color(0x33E9C46A),
                        titulo = "Generar código QR",
                        descripcion = "Escanea para unirte",
                        icono = Icons.Default.QrCode,
                        onClick = {
                            mostrarQR = !mostrarQR
                            Log.i("MyApp", "QR toggled: $mostrarQR")
                        }
                    )

                    if (mostrarQR) {
                        CodigoQR()
                    }

                    BotonOpcion(
                        emoji = null,
                        iconoColor = Color(0xFF22C55E),
                        fondoIcono = Color(0x3322C55E),
                        titulo = "Compartir enlace",
                        descripcion = "WhatsApp, Telegram, etc.",
                        icono = Icons.Default.Share,
                        onClick = {
                            Log.i("MyApp", "Compartir enlace clicked")
                        }
                    )

                    InfoNFC()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1AFFFFFF))
                )

                Button(
                    onClick = {
                        Log.i("MyApp", "Cerrar InvitarNFC clicked")
                        onCerrar()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(bottom = 8.dp)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HeaderInvitar(onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Invitar amigos",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0x1AFFFFFF), CircleShape)
                .clickable { onCerrar() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0x1AFFFFFF))
    )
}

@Composable
fun BotonNFC(escaneando: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A9D8F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!escaneando) onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Nfc,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (escaneando) "Acercando teléfonos..." else "Acercar teléfonos",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Únete a carrera en curso por NFC",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SeparadorO() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )
        Text(
            "o compartir con",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )
    }
}

@Composable
fun BotonOpcion(
    emoji: String?,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    iconoColor: Color,
    fondoIcono: Color,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(fondoIcono, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = iconoColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    descripcion,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CodigoQR() {
    val celdas = remember {
        List(64) { Random.nextBoolean() }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (fila in 0 until 8) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (col in 0 until 8) {
                                val index = fila * 8 + col
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (celdas[index]) Color.Black else Color.White,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Escanea este código para unirte a la carrera",
                color = Color.Black,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InfoNFC() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1A2A9D8F)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D2A9D8F))
    ) {
        Text(
            "💡 Quien escanee el QR o enlace se unirá automáticamente a tu carrera activa",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
    }
}