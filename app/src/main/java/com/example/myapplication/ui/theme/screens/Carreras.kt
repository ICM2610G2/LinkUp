package com.example.myapplication.ui.theme.screens

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
            .background(Color(0xFF0B0B0B))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item { CarreraActivaBadge() }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item { HeaderRuta() }

        item { Spacer(modifier = Modifier.height(24.dp)) }

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

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("32 min", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Icon(Icons.Default.Place, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("5 puntos", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Icon(Icons.Default.Group, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("4 jugadores", color = Color.Gray, fontSize = 14.sp)
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
            Text("Progreso", color = Color.White)
            Text("2/5", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
        progress = { 0.4f },
        modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
        color = Color(0xFFFF9800),
        trackColor = Color(0xFF2A2A2A),
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun PuntosDeControlSection() {

    Column {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF9800))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Puntos de control",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PuntoItem("Museo del Oro", "14:23", completado = true)
        PuntoItem("Iglesia de San Francisco", "14:41", completado = true)
        PuntoItem("Plaza de Bolívar", "", siguiente = true)
        PuntoItem("La Candelaria", "")
        PuntoItem("Torre Colpatria", "")
    }
}

@Composable
fun PuntoItem(
    titulo: String,
    hora: String,
    completado: Boolean = false,
    siguiente: Boolean = false
) {

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (completado) Color(0xFFFF9800) else Color(0xFF2A2A2A),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completado) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    titulo,
                    color = if (completado) Color.White else Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                if (hora.isNotEmpty()) {
                    Text(
                        hora,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            if (siguiente) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Siguiente")
                }
            }
        }
    }
}

@Composable
fun ClasificacionSection() {

    Column {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Clasificación",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RankingItem(1, "Camila R.", "CR")
        RankingItem(2, "Andrés M.", "AM")
    }
}

@Composable
fun RankingItem(
    posicion: Int,
    nombre: String,
    iniciales: String
) {

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                posicion.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                nombre,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Text(
                "3 pts",
                color = Color.Gray
            )
        }
    }
}
