package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.Checkpoint
import com.example.myapplication.repository.RaceRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

@Composable
fun CrearCarrera(
    onCerrar: () -> Unit,
    onCarreraCreada: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val checkpointsDemo = remember {
        listOf(
            Checkpoint(
                name = "Museo del Oro",
                description = "Uno de los museos más importantes de Bogotá.",
                coordinates = GeoPoint(4.6019, -74.0720),
                geofenceRadiusM = 50,
                points = 100,
                order = 1
            ),
            Checkpoint(
                name = "Plaza de Bolívar",
                description = "Centro histórico y político de Colombia.",
                coordinates = GeoPoint(4.5981, -74.0758),
                geofenceRadiusM = 50,
                points = 100,
                order = 2
            ),
            Checkpoint(
                name = "Chorro de Quevedo",
                description = "Lugar emblemático de La Candelaria.",
                coordinates = GeoPoint(4.5973, -74.0695),
                geofenceRadiusM = 50,
                points = 100,
                order = 3
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HeaderCrearCarreraReal(onCerrar = onCerrar)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        CampoTextoCarrera(
                            titulo = "Nombre de la carrera",
                            valor = nombre,
                            placeholder = "Ej: Ruta La Candelaria",
                            onChange = { nombre = it }
                        )
                    }

                    item {
                        CampoTextoCarrera(
                            titulo = "Descripción",
                            valor = descripcion,
                            placeholder = "Describe la ruta y el objetivo de la carrera",
                            onChange = { descripcion = it },
                            multilinea = true
                        )
                    }

                    item {
                        Text(
                            "Visibilidad",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OpcionVisibilidad(
                                titulo = "Pública",
                                activa = isPublic,
                                icon = Icons.Default.Public,
                                modifier = Modifier.weight(1f),
                                onClick = { isPublic = true }
                            )

                            OpcionVisibilidad(
                                titulo = "Privada",
                                activa = !isPublic,
                                icon = Icons.Default.Lock,
                                modifier = Modifier.weight(1f),
                                onClick = { isPublic = false }
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Place,
                                        null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Checkpoints seleccionados",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                checkpointsDemo.forEachIndexed { index, checkpoint ->
                                    Text(
                                        "${index + 1}. ${checkpoint.name}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Por ahora usamos puntos demo. Después los seleccionaremos desde el mapa.",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            Text(
                                errorMessage!!,
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            val result = raceRepository.createRace(
                                name = nombre,
                                description = descripcion,
                                isPublic = isPublic,
                                checkpoints = checkpointsDemo
                            )

                            result.fold(
                                onSuccess = {
                                    isLoading = false
                                    onCarreraCreada()
                                },
                                onFailure = { e ->
                                    isLoading = false
                                    errorMessage = e.message ?: "No se pudo crear la carrera"
                                }
                            )
                        }
                    },
                    enabled = nombre.isNotBlank() && descripcion.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Crear carrera",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCrearCarreraReal(onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Crear carrera",
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
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun CampoTextoCarrera(
    titulo: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit,
    multilinea: Boolean = false
) {
    Column {
        Text(
            titulo,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (multilinea) 110.dp else 56.dp),
            placeholder = {
                Text(placeholder, color = Color.White.copy(alpha = 0.35f))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF9800),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = !multilinea,
            maxLines = if (multilinea) 4 else 1
        )
    }
}

@Composable
fun OpcionVisibilidad(
    titulo: String,
    activa: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activa) Color(0xFFFF9800) else Color(0xFF252525)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (activa) Color.White else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                titulo,
                color = if (activa) Color.White else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}