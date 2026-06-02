package com.example.myapplication.screens.homeScreens

import android.util.Log
import com.example.myapplication.R
import androidx.compose.runtime.Composable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.models.Race
import com.example.myapplication.data.models.RaceSession
import com.example.myapplication.model.HomeViewModel
import com.example.myapplication.screens.CrearPunto
import com.example.myapplication.screens.GaleriaLugar
import com.example.myapplication.screens.perfilScreens.ListaAmigos
import com.example.myapplication.screens.MenuFlotante

@Composable
fun Home(
    onEscanearQR: () -> Unit,
    onAbrirLobby: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.homeState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarRecentSessions()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B0B)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HeaderSection() }
            
            // DYNAMIC RECENT LOBBIES SECTION
            if (state.recentSessions.isNotEmpty()) {
                item {
                    Text(
                        "Tus carreras recientes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
                items(state.recentSessions) { item ->
                    HomeRaceCard(
                        session = item.session,
                        race = item.race,
                        onClick = { onAbrirLobby(item.session.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            } else if (state.isLoadingSessions) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { CarreraEnCursoCard() } // Fallback card
            }

            item {
                RutasHeader({
                    viewModel.updateMostrarCrearCarrera(true)
                })
            }
            item {
                RutaItem(
                    titulo = "La Candelaria",
                    distancia = "2.5 km",
                    puntos = "12",
                    dificultad = "Media",
                    dificultadColor = Color(0xFFFFB300),
                    imageRes = R.drawable.la_candelaria,
                    onClick = {
                        viewModel.updateLugarSeleccionado("La Candelaria")
                    }
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
                    onClick = {
                        viewModel.updateLugarSeleccionado("Monserrate")
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                InvitarAmigosCard(onInvitar = {
                    viewModel.updateMostrarNFC(true)
                })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .size(56.dp)
                .background(Color(0xFF2A9D8F), CircleShape)
                .clickable {
                    viewModel.updateMostrarMenuFlotante(true)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }

        if (state.mostrarMenuFlotante) {
            MenuFlotante(
                onCerrar = { viewModel.updateMostrarMenuFlotante(false) },
                onCrearCarrera = { viewModel.updateMostrarCrearCarrera(true) },
                onInvitarNFC = { viewModel.updateMostrarNFC(true) },
                onVerAmigos = { viewModel.updateMostrarAmigos(true) }
            )
        }

        if (state.mostrarCrearCarrera) {
            CrearCarrera(
                onCerrar = { viewModel.updateMostrarCrearCarrera(false) },
                onCarreraCreada = {
                    viewModel.updateMostrarCrearCarrera(false)
                    viewModel.cargarRecentSessions()
                }
            )
        }

        if (state.mostrarNFC) {
            InvitarNFC(
                onCerrar = { viewModel.updateMostrarNFC(false) },
                onEscanearQR = {
                    viewModel.updateMostrarNFC(false)
                    onEscanearQR()
                }
            )
        }

        if (state.mostrarAmigos) {
            ListaAmigos(onCerrar = { viewModel.updateMostrarAmigos(false) })
        }

        if (state.lugarSeleccionado != null) {
            GaleriaLugar(
                nombreLugar = state.lugarSeleccionado!!,
                onCerrar = { viewModel.updateLugarSeleccionado(null) }
            )
        }
    }
}

@Composable
fun HomeRaceCard(
    session: RaceSession,
    race: Race?,
    onClick: () -> Unit
) {
    val difficultyColor = when (race?.difficulty) {
        "facil" -> Color(0xFF22C55E)
        "media" -> Color(0xFFEAB308)
        "dificil" -> Color(0xFFEF4444)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0x12FFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x26FF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    if (race?.photoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = race.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Route,
                            null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        race?.name ?: session.raceName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Estado: ${session.status.uppercase()}",
                        color = Color(0xFFFF9800),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }

            if (race != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeCardInfoChip(
                        text = "${"%.1f".format(race.estimatedDistanceKm)} km",
                        icon = Icons.Default.Place
                    )

                    HomeCardInfoChip(
                        text = "${race.checkpointCount} puntos",
                        icon = Icons.Default.EmojiEvents
                    )

                    Box(
                        modifier = Modifier
                            .background(difficultyColor, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            race.difficulty.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCardInfoChip(
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
fun StatBadge(valor: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
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

@Composable
fun CarreraEnCursoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0x1AFF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Route, null, tint = Color(0xFFFF9800))
            }
            Column {
                Text(
                    "No hay carreras activas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "¡Explora rutas y únete a una!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RutasHeader(onCrearRuta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
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
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
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
fun InvitarAmigosCard(onInvitar: () -> Unit) {
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
                "Código de amistad, QR o NFC",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onInvitar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.GroupAdd, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invitar Amigos")
            }
        }
    }
}
