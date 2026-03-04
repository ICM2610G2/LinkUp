package com.example.myapplication.ui.theme.screens

import android.annotation.SuppressLint
import com.example.myapplication.R
import com.example.myapplication.ui.theme.model.GpsPoint

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Mapa() {

    // Genera puntos random una sola vez
    val gpsPoints = remember {
        List(15) {
            GpsPoint(
                xPercent = Random.nextFloat(),
                yPercent = Random.nextFloat(),
                isActive = Random.nextBoolean()
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
    ) {

        item {

            BoxWithConstraints(
                modifier = Modifier
                    .fillParentMaxHeight()
                    .fillMaxWidth()
            ) {

                // Imagen de fondo (mapa)
                Image(
                    painter = painterResource(id = R.drawable.map_background),
                    contentDescription = "Mapa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Punto central destacado
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color(0x33FF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                // Puntos GPS random posicionados correctamente
                gpsPoints.forEach { point ->

                    val xPos = maxWidth * point.xPercent
                    val yPos = maxHeight * point.yPercent

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (point.isActive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFF9CA3AF),
                        modifier = Modifier
                            .offset(x = xPos, y = yPos)
                            .size(28.dp)
                    )
                }

                // Card superior (ligeramente más abajo)
                DestinationCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                )

                // Card inferior (respeta barra + espacio adicional)
                StatusCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .offset(y = (-100).dp) // <-- sube la card
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DestinationCard(modifier: Modifier = Modifier) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1F)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text("Destino", color = Color.Gray, fontSize = 12.sp)
                Text(
                    "Plaza de Bolívar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "350m • 4 min",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun StatusCard(modifier: Modifier = Modifier) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1A1A1F)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text("Brújula", color = Color.Gray, fontSize = 12.sp)
            Text(
                "N 23° NE",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {

                InfoBox(
                    title = "Velocidad",
                    value = "5.2 km/h",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                InfoBox(
                    title = "Movimiento",
                    value = "Activo ✓",
                    valueColor = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validar llegada con foto")
            }
        }
    }
}

@Composable
fun InfoBox(
    title: String,
    value: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF121217)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}

