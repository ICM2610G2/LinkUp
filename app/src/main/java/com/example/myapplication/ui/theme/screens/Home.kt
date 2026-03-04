package com.example.myapplication.ui.theme.screens


import com.example.myapplication.R

import androidx.compose.runtime.Composable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Home () {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item {
            HeaderSection()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatsSection()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            CarreraEnCursoCard()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            RutasHeader({  })
        }

        item {
            RutaItem(
                titulo = "La Candelaria",
                distancia = "2.5 km",
                puntos = "12",
                dificultad = "Media",
                dificultadColor = Color(0xFFFFB300),
                onClick = {}
            )
        }

        item {
            RutaItem(
                titulo = "Monserrate",
                distancia = "4.8 km",
                puntos = "5",
                dificultad = "Difícil",
                dificultadColor = Color(0xFFE53935),
                onClick = {}
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            InvitarCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
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
                        listOf(Color.Transparent, Color(0xFF0B0B0B))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = "Bienvenido de vuelta",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Row {
                Text(
                    text = "Explora ",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bogotá",
                    color = Color(0xFFFF9800),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard("3", "Racha", Icons.Default.LocalFireDepartment)
        StatCard("12", "Lugares", Icons.Default.Place)
        StatCard("48", "Posición", Icons.AutoMirrored.Filled.TrendingUp)
    }
}

@Composable
fun StatCard(valor: String, label: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(110.dp)
            .height(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFF9800))
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun CarreraEnCursoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Carrera en curso",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "La Candelaria · 3 de 5 puntos",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun RutasHeader(onCrearRuta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Rutas destacadas",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Button(
            onClick = onCrearRuta,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("+ Crear")
        }
    }
}

@Composable
fun RutaItem(
    titulo: String,
    distancia: String,
    puntos: String,
    dificultad: String,
    dificultadColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(titulo, color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "$distancia · $puntos puntos",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(dificultadColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    dificultad,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InvitarCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                "Invita a tus amigos",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Comparte un grupo por NFC y explora juntos",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Nfc, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir con NFC")
            }
        }
    }
}
