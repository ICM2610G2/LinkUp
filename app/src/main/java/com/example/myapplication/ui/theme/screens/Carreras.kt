package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.remote.creation.compose.state.log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Carreras(){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { CarreraActivaBadge() }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { HeaderRuta() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { ProgresoSection() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { PuntosDeControlSection() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { ClasificacionSection() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun CarreraActivaBadge() {
    Box(
        modifier = Modifier
            .background(
                Color(0xFFFF9800),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            "CARRERA ACTIVA",
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeaderRuta() {
    Column {
        Text(
            text = "Ruta Centro Histórico",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("32 min", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Place,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("5 puntos", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("4 jugadores", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

            }
        }
    }
}

@Composable
fun ProgresoSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progreso", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text("2/5", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
        progress = { 0.4f },
        modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
        color = Color(0xFFFF9800),
        trackColor = Color(0xFF1A1A1A),
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun PuntosDeControlSection() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn,
                null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Puntos de control",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val checkpoints = listOf(
            Triple("Museo del Oro", "14:23", true),
            Triple("Iglesia de San Francisco", "14:41", true),
            Triple("Plaza de Bolívar", "", false),
            Triple("La Candelaria", "", false),
            Triple("Torre Colpatria", "", false)
        )

        checkpoints.forEachIndexed { index, (nombre, hora, completado) ->
            PuntoItem(
                titulo = nombre,
                hora = hora,
                completado = completado,
                siguiente = !completado && index == 2,
                tieneConector = index < checkpoints.size - 1
            )
        }
    }
}

@Composable
fun PuntoItem(
    titulo: String,
    hora: String,
    completado: Boolean = false,
    siguiente: Boolean = false,
    tieneConector: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (completado) Color(0xFFFF9800) else Color(0xFF252525),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completado) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            if (tieneConector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            if (completado) Color(0xFFFF9800) else Color(0x1AFFFFFF)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (tieneConector) 0.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        titulo,
                        color = if (completado) Color.White else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    if (hora.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(hora, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Foto ✓", color = Color(0xFF22C55E), fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (siguiente) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF9800), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Siguiente",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (tieneConector) {
        Spacer(modifier = Modifier.height(0.dp))
    } else {
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ClasificacionSection() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star,
                null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Clasificación",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RankingItem(1, "Camila R.", "CR", avatarColor = Color(0xFFFF9800), esYo = false)
        RankingItem(2, "Andrés M.", "AM", avatarColor = Color(0xFF4B4B4B), esYo = false)
        RankingItem(3, "TÚ", "TÚ", avatarColor = Color(0xFFFFA500), esYo = true)
        RankingItem(4, "Laura P.", "LP", avatarColor = Color(0xFF3A3A3A), esYo = false)
    }
}

@Composable
fun RankingItem(
    posicion: Int,
    nombre: String,
    iniciales: String,
    avatarColor: Color,
    esYo: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = if (esYo) {
            BorderStroke(1.dp, Color(0xFFFF9800))
        } else {
            BorderStroke(1.dp, Color(0x0DFFFFFF))
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                posicion.toString(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.width(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    iniciales,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                nombre,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "3 pts",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}
