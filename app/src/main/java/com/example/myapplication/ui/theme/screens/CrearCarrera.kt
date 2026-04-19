package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.places.Place

@Composable
fun CrearCarrera(
    onCerrar: () -> Unit,
    onIniciar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipoOrden by remember { mutableStateOf("libre") }
    var tiempoLimite by remember { mutableStateOf("sin-limite") }
    var tipoCarrera by remember { mutableStateOf("rapida") }
    var lugaresSeleccionados by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderCrearCarrera(onCerrar = {
                    Log.i("MyApp", "Cerrar crear carrera clicked")
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
                        SeccionNombre(
                            nombre = nombre,
                            onNombreChange = { nombre = it }
                        )
                    }
                    item {
                        SeccionTipoOrden(
                            tipoOrden = tipoOrden,
                            onTipoOrdenChange = {
                                tipoOrden = it
                                Log.i("MyApp", "Tipo orden seleccionado: $it")
                            }
                        )
                    }
                    item {
                        SeccionTiempoLimite(
                            tiempoLimite = tiempoLimite,
                            onTiempoChange = {
                                tiempoLimite = it
                                Log.i("MyApp", "Tiempo límite seleccionado: $it")
                            }
                        )
                    }
                    item {
                        SeccionTipoCarrera(
                            tipoCarrera = tipoCarrera,
                            onTipoCarreraChange = {
                                tipoCarrera = it
                                Log.i("MyApp", "Tipo carrera seleccionado: $it")
                            }
                        )
                    }
                    item {
                        SeccionLugares(
                            lugaresSeleccionados = lugaresSeleccionados,
                            onAgregarLugar = {
                                lugaresSeleccionados = minOf(lugaresSeleccionados + 1, 10)
                                Log.i("MyApp", "Lugares seleccionados: $lugaresSeleccionados")
                            }
                        )
                    }
                }
                FooterCrearCarrera(
                    habilitado = nombre.isNotBlank() && lugaresSeleccionados >= 2,
                    onIniciar = {
                        Log.i("MyApp", "Iniciar carrera clicked: $nombre")
                        onIniciar()
                        onCerrar()
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderCrearCarrera(onCerrar: () -> Unit) {
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
fun SeccionNombre(nombre: String, onNombreChange: (String) -> Unit) {
    Column {
        Text("Nombre de la carrera", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = nombre,
            onValueChange = onNombreChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            placeholder = {
                Text(
                    "Ej: Aventura en La Candelaria",
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
            singleLine = true
        )
    }
}

@Composable
fun SeccionTipoOrden(tipoOrden: String, onTipoOrdenChange: (String) -> Unit) {
    val opciones = listOf(
        Triple("libre", "Orden libre", "Visita en cualquier orden"),
        Triple("fijo", "Ruta fija", "Orden obligatorio")
    )
    Column {
        Text("Tipo de orden", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            opciones.forEach { (id, titulo, descripcion) ->
                val seleccionado = tipoOrden == id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionado) Color(0x332A9D8F) else Color(0xFF252525)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTipoOrdenChange(id) },
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (seleccionado) Color(0xFF2A9D8F) else Color(0x33FFFFFF)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            titulo,
                            color = if (seleccionado) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            descripcion,
                            color = if (seleccionado) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionTiempoLimite(tiempoLimite: String, onTiempoChange: (String) -> Unit) {
    val opciones = listOf("15", "30", "45", "60", "sin-limite")
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tiempo límite", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opciones.forEach { tiempo ->
                val seleccionado = tiempoLimite == tiempo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (seleccionado) Color(0xFFE9C46A) else Color(0xFF252525),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onTiempoChange(tiempo) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (tiempo == "sin-limite") "Sin límite" else "$tiempo min",
                        color = if (seleccionado) Color.Black else Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionTipoCarrera(tipoCarrera: String, onTipoCarreraChange: (String) -> Unit) {
    val tipos = listOf(
        Triple("rapida", "⚡ Rápida", "Gana el primero"),
        Triple("exploracion", "🧭 Exploración", "Fotos y detalles"),
        Triple("fotografica", "📸 Fotográfica", "Mejores fotos")
    )
    Column {
        Text("Tipo de carrera", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tipos.forEach { (id, etiqueta, descripcion) ->
                val seleccionado = tipoCarrera == id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (seleccionado) Color(0x33E9C46A) else Color(0xFF252525)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTipoCarreraChange(id) },
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (seleccionado) Color(0xFFE9C46A) else Color(0x33FFFFFF)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            etiqueta,
                            color = if (seleccionado) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            descripcion,
                            color = if (seleccionado) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionLugares(lugaresSeleccionados: Int, onAgregarLugar: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Lugares seleccionados: $lugaresSeleccionados",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAgregarLugar,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Text("Seleccionar lugares en el mapa", color = Color.White)
        }
        if (lugaresSeleccionados < 2) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "* Debes seleccionar al menos 2 lugares",
                color = Color(0xFFE9C46A),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FooterCrearCarrera(habilitado: Boolean, onIniciar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )
        Button(
            onClick = onIniciar,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2A9D8F),
                disabledContainerColor = Color(0xFF2A9D8F).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Iniciar carrera",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
