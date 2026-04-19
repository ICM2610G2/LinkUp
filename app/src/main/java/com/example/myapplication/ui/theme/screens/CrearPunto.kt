package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrearPunto(
    onCerrar: () -> Unit,
    onPublicar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("cultural") }
    var dificultad by remember { mutableStateOf("facil") }
    var obteniendo by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HeaderCrearPunto(onCerrar = {
                    Log.i("MyApp", "Cerrar crear punto clicked")
                    onCerrar()
                })

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    item {
                        SeccionNombrePunto(
                            nombre = nombre,
                            onNombreChange = { nombre = it }
                        )
                    }

                    item {
                        SeccionDescripcion(
                            descripcion = descripcion,
                            onDescripcionChange = { descripcion = it }
                        )
                    }

                    item {
                        SeccionCategoria(
                            categoria = categoria,
                            onCategoriaChange = {
                                categoria = it
                                Log.i("MyApp", "Categoria seleccionada: $it")
                            }
                        )
                    }

                    item {
                        SeccionDificultad(
                            dificultad = dificultad,
                            onDificultadChange = {
                                dificultad = it
                                Log.i("MyApp", "Dificultad seleccionada: $it")
                            }
                        )
                    }

                    item {
                        SeccionUbicacion(
                            ubicacion = ubicacion,
                            obteniendo = obteniendo,
                            onObtenerUbicacion = {
                                obteniendo = true
                                Log.i("MyApp", "Obtener ubicación clicked")
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    ubicacion = Pair(4.5981, -74.0758)
                                    obteniendo = false
                                    Log.i("MyApp", "Ubicación obtenida")
                                }, 1000)
                            }
                        )
                    }

                    item { ConsejoPunto() }
                }

                FooterCrearPunto(
                    habilitado = nombre.isNotBlank() && descripcion.isNotBlank() && ubicacion != null,
                    onPublicar = {
                        Log.i("MyApp", "Publicar punto clicked: $nombre")
                        onPublicar()
                        onCerrar()
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderCrearPunto(onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Crear punto",
            color = Color.White,
            fontSize = 22.sp,
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
fun SeccionNombrePunto(nombre: String, onNombreChange: (String) -> Unit) {
    Column {
        Text(
            "Nombre del lugar",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = nombre,
            onValueChange = onNombreChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            placeholder = {
                Text("Ej: Mirador de Monserrate", color = Color.White.copy(alpha = 0.4f))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
                cursorColor = Color(0xFFFF9800),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun SeccionDescripcion(descripcion: String, onDescripcionChange: (String) -> Unit) {
    Column {
        Text(
            "Descripción",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = {
                Text(
                    "Describe este lugar y por qué vale la pena visitarlo...",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
                cursorColor = Color(0xFFFF9800),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
    }
}

@Composable
fun SeccionCategoria(categoria: String, onCategoriaChange: (String) -> Unit) {
    val categorias = listOf(
        Triple("cultural", "Cultural", "🏛️"),
        Triple("naturaleza", "Naturaleza", "🌳"),
        Triple("gastronomia", "Gastronomía", "🍽️")
    )

    Column {
        Text(
            "Categoría",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { (id, label, emoji) ->
                val seleccionada = categoria == id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionada) Color(0xFFFF9800) else Color(0xFF252525)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategoriaChange(id) },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (seleccionada) Color(0xFFFF9800) else Color(0x1AFFFFFF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            label,
                            color = if (seleccionada) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionDificultad(dificultad: String, onDificultadChange: (String) -> Unit) {
    val dificultades = listOf(
        Triple("facil", "Fácil", Color(0xFF16A34A)),
        Triple("media", "Media", Color(0xFFCA8A04)),
        Triple("dificil", "Difícil", Color(0xFFEA580C))
    )

    Column {
        Text(
            "Dificultad",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dificultades.forEach { (id, label, color) ->
                val seleccionada = dificultad == id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionada) color else Color(0xFF252525)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDificultadChange(id) },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (seleccionada) color else Color(0x1AFFFFFF)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionUbicacion(
    ubicacion: Pair<Double, Double>?,
    obteniendo: Boolean,
    onObtenerUbicacion: () -> Unit
) {
    Column {
        Text(
            "Ubicación",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (ubicacion == null) {
            Button(
                onClick = onObtenerUbicacion,
                enabled = !obteniendo,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
            ) {
                if (obteniendo) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obteniendo ubicación...", color = Color.White)
                } else {
                    Icon(Icons.Default.Place, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Usar ubicación actual", color = Color.White)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D22C55E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Place, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                        Text(
                            "Ubicación capturada",
                            color = Color(0xFF22C55E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${ubicacion.first}, ${ubicacion.second}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ConsejoPunto() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "💡 Consejo: Toma una foto del lugar después de crearlo",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Otros usuarios verán tu punto en el mapa y podrán visitarlo para ganar puntos",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FooterCrearPunto(habilitado: Boolean, onPublicar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )

        Button(
            onClick = onPublicar,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Publicar punto",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}