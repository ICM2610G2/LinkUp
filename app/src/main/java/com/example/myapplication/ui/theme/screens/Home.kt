package com.example.myapplication.ui.theme.screens


import android.util.Log
import com.example.myapplication.R

import androidx.compose.runtime.Composable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var mostrarCrearPunto by remember { mutableStateOf(false) }
    var mostrarMenuFlotante by remember { mutableStateOf(false) }
    var mostrarCrearCarrera by remember { mutableStateOf(false) }
    var mostrarNFC by remember { mutableStateOf(false) }
    var mostrarAmigos by remember { mutableStateOf(false) }
    var lugarSeleccionado by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B0B)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HeaderSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { CarreraEnCursoCard() }
            item {
                RutasHeader({
                    Log.i("MyApp", "Crear ruta")
                    mostrarCrearCarrera = true})
            }
            item {
                RutaItem(
                    titulo = "La Candelaria",
                    distancia = "2.5 km",
                    puntos = "12",
                    dificultad = "Media",
                    dificultadColor = Color(0xFFFFB300),
                    imageRes = R.drawable.la_candelaria,
                    onClick = {Log.i("MyApp", "La Candelaria")
                    lugarSeleccionado = "La Candelaria"}
                )
            }
            item {
                RutaItem(
                    titulo = "Monserrate",
                    distancia = "4.8 km",
                    puntos = "5",
                    dificultad = "Difícil",
                    dificultadColor = Color(0xFFE53935),
                    imageRes = R.drawable.monserrate,
                    onClick = {Log.i("MyApp", "Monserrate")
                    lugarSeleccionado = "Monserrate"}
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                InvitarCard(onNFC = {
                    Log.i("MyApp", "Compartir NFC")
                    mostrarNFC = true
                })
            }
            item{Spacer(modifier = Modifier.height(24.dp))}
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .size(56.dp)
                .background(Color(0xFF2A9D8F), CircleShape)
                .clickable {
                    Log.i("MyApp", "Menu flotante clicked")
                    mostrarMenuFlotante = true
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }

        // Modales
        if (mostrarMenuFlotante) {
            MenuFlotante(
                onCerrar = { mostrarMenuFlotante = false },
                onCrearCarrera = { mostrarCrearCarrera = true },
                onInvitarNFC = { mostrarNFC = true },
                onVerAmigos = { mostrarAmigos = true }
            )
        }

        if (mostrarCrearCarrera) {
            CrearCarrera(
                onCerrar = { mostrarCrearCarrera = false },
                onIniciar = {
                    Log.i("MyApp", "Carrera iniciada desde Home")
                    mostrarCrearCarrera = false
                }
            )
        }

        if (mostrarCrearPunto) {
            CrearPunto(
                onCerrar = { mostrarCrearPunto = false },
                onPublicar = {
                    Log.i("MyApp", "Punto publicado desde Home")
                    mostrarCrearPunto = false
                }
            )
        }

        if (mostrarNFC) {
            InvitarNFC(onCerrar = { mostrarNFC = false })
        }

        if (mostrarAmigos) {
            ListaAmigos(onCerrar = { mostrarAmigos = false })
        }

        if (lugarSeleccionado != null) {
            GaleriaLugar(
                nombreLugar = lugarSeleccionado!!,
                onCerrar = { lugarSeleccionado = null }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.bogota_bg),
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
                            Color(0x80000000),
                            Color(0x4D000000),
                            Color(0xFF0A0A0A)
                        )
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
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBadge("3", "Racha", Icons.Default.LocalFireDepartment, modifier = Modifier.weight(1f))
                StatBadge("12", "Lugares", Icons.Default.Place, modifier = Modifier.weight(1f))
                StatBadge("48", "Posición", Icons.AutoMirrored.Filled.TrendingUp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatBadge(valor: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Color(0x4D000000),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

//@Composable
//fun StatCard(valor: String, label: String, icon: ImageVector) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier
//            .width(110.dp)
//            .height(90.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Icon(icon, contentDescription = null, tint = Color(0xFFFF9800))
//            Text(valor, color = Color.White, fontWeight = FontWeight.Bold)
//            Text(label, color = Color.Gray, fontSize = 12.sp)
//        }
//    }
//}

@Composable
fun CarreraEnCursoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { Log.i("MyApp", "Carrera en curso clicked")}
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x33FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = Color.White)
                }
            Column {
                Text(
                    "Carrera en curso",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "La Candelaria · 3 de 5 puntos",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Crear", fontSize = 12.sp)
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
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(distancia, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "$puntos fotos",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .background(dificultadColor, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        dificultad,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun InvitarCard(onNFC: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Invita a tus amigos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Comparte un grupo por NFC y explora juntos",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNFC,
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
