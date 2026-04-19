package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ListaAmigos(onCerrar: () -> Unit) {

    var tabActiva by remember { mutableStateOf("todos") }

    val amigos = listOf(
        Triple("Carlos Ruiz", "CR", true) to "En carrera",
        Triple("Ana Martínez", "AM", true) to "Explorando",
        Triple("Luis Pérez", "LP", false) to "Desconectado",
        Triple("María González", "MG", true) to "En línea"
    )

    val solicitudes = listOf(
        Pair("Sofia Ramírez", "SR") to 3,
        Pair("Diego Castro", "DC") to 7
    )

    val cercanos = listOf(
        Triple("Carlos Ruiz", "CR", 5) to 12,
        Triple("Ana Martínez", "AM", 4) to 8
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HeaderAmigos(
                    tabActiva = tabActiva,
                    totalAmigos = amigos.size,
                    totalSolicitudes = solicitudes.size,
                    totalCercanos = cercanos.size,
                    onTabChange = {
                        tabActiva = it
                        Log.i("MyApp", "Tab amigos: $it")
                    },
                    onCerrar = {
                        Log.i("MyApp", "Cerrar lista amigos clicked")
                        onCerrar()
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    when (tabActiva) {
                        "todos" -> items(amigos) { (info, estado) ->
                            ItemAmigo(
                                nombre = info.first,
                                iniciales = info.second,
                                enLinea = info.third,
                                estado = estado
                            )
                        }
                        "solicitudes" -> {
                            if (solicitudes.isEmpty()) {
                                item { EstadoVacioSolicitudes() }
                            } else {
                                items(solicitudes) { (info, mutuos) ->
                                    ItemSolicitud(
                                        nombre = info.first,
                                        iniciales = info.second,
                                        amigosMutuos = mutuos
                                    )
                                }
                            }
                        }
                        "cercanos" -> items(cercanos) { (info, carreras) ->
                            ItemCercano(
                                nombre = info.first,
                                iniciales = info.second,
                                corazones = info.third,
                                carreras = carreras
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderAmigos(
    tabActiva: String,
    totalAmigos: Int,
    totalSolicitudes: Int,
    totalCercanos: Int,
    onTabChange: (String) -> Unit,
    onCerrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Amigos",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabAmigos(
                label = "Todos ($totalAmigos)",
                activa = tabActiva == "todos",
                color = Color(0xFF2A9D8F),
                textColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onTabChange("todos") }
            )
            TabAmigos(
                label = "Solicitudes ($totalSolicitudes)",
                activa = tabActiva == "solicitudes",
                color = Color(0xFFE9C46A),
                textColor = Color.Black,
                modifier = Modifier.weight(1f),
                onClick = { onTabChange("solicitudes") }
            )
            TabAmigos(
                label = "Cercanos ($totalCercanos)",
                activa = tabActiva == "cercanos",
                color = Color(0xFFEF4444),
                textColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onTabChange("cercanos") }
            )
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
fun TabAmigos(
    label: String,
    activa: Boolean,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (activa) color else Color(0xFF1A1A1A),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (activa) textColor else Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ItemAmigo(
    nombre: String,
    iniciales: String,
    enLinea: Boolean,
    estado: String
) {
    var mostrarMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0DFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2A9D8F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (enLinea) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    estado,
                    color = if (enLinea) Color(0xFF22C55E) else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                        .clickable {
                            mostrarMenu = !mostrarMenu
                            Log.i("MyApp", "Menu amigo clicked: $nombre")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }

                if (mostrarMenu) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(top = 36.dp)
                            .width(120.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                    ) {
                        Column {
                            Text(
                                "Ver perfil",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Log.i("MyApp", "Ver perfil: $nombre")
                                        mostrarMenu = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            Text(
                                "Bloquear",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Log.i("MyApp", "Bloquear: $nombre")
                                        mostrarMenu = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            Text(
                                "Reportar",
                                color = Color(0xFFE9C46A),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Log.i("MyApp", "Reportar: $nombre")
                                        mostrarMenu = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSolicitud(nombre: String, iniciales: String, amigosMutuos: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DE9C46A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE9C46A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "$amigosMutuos amigos en común",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { Log.i("MyApp", "Aceptar solicitud: $nombre") },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9D8F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aceptar", fontSize = 12.sp)
                }

                Button(
                    onClick = { Log.i("MyApp", "Rechazar solicitud: $nombre") },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ItemCercano(nombre: String, iniciales: String, corazones: Int, carreras: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1AEF4444)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DEF4444))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("$carreras carreras juntos", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(corazones) {
                    Text("❤️", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun EstadoVacioSolicitudes() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.PersonAdd,
            null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            "No tienes solicitudes pendientes",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}