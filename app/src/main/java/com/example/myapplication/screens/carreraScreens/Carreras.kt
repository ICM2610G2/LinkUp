package com.example.myapplication.screens.carreraScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.Race
import com.example.myapplication.repository.RaceRepository
import kotlinx.coroutines.launch

@Composable
fun Carreras(
    onCrearCarrera: () -> Unit,
    onAbrirLobby: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }

    var carreras by remember { mutableStateOf<List<Race>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun cargarCarreras() {
        isLoading = true
        carreras = raceRepository.getPublicRaces()
        isLoading = false
    }

    LaunchedEffect(Unit) {
        cargarCarreras()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeaderCarreras(
                    onCrearCarrera = onCrearCarrera,
                    onRefresh = {
                        scope.launch { cargarCarreras() }
                    }
                )
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF9800))
                        }
                    }
                }

                carreras.isEmpty() -> {
                    item {
                        EstadoVacioCarreras(onCrearCarrera = onCrearCarrera)
                    }
                }

                else -> {
                    items(carreras, key = { it.id }) { carrera ->
                        CarreraPublicaCard(
                            carrera = carrera,
                            onClick = {
                                scope.launch {
                                    val result = raceRepository.findOrJoinLobby(carrera)
                                    result.onSuccess { sessionId ->
                                        onAbrirLobby(sessionId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCarreras(
    onCrearCarrera: () -> Unit,
    onRefresh: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Carreras",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Explora rutas públicas o crea una nueva",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color.White
                    )
                }

                Button(
                    onClick = onCrearCarrera,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun EstadoVacioCarreras(
    onCrearCarrera: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0x26FF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "No hay carreras públicas todavía",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Crea la primera ruta para que otros jugadores puedan unirse.",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onCrearCarrera,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Crear carrera")
            }
        }
    }
}

@Composable
fun CarreraPublicaCard(
    carrera: Race,
    onClick: () -> Unit
) {
    val difficultyColor = when (carrera.difficulty) {
        "facil" -> Color(0xFF22C55E)
        "media" -> Color(0xFFEAB308)
        "dificil" -> Color(0xFFEF4444)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0x12FFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0x26FF9800), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Route,
                        null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        carrera.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (carrera.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            carrera.description,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    text = "${"%.1f".format(carrera.estimatedDistanceKm)} km",
                    icon = Icons.Default.Place
                )

                InfoChip(
                    text = "${carrera.checkpointCount} puntos",
                    icon = Icons.Default.EmojiEvents
                )

                Box(
                    modifier = Modifier
                        .background(difficultyColor, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        carrera.difficulty.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    text: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF252525), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp
        )
    }
}