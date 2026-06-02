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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Home(
    onEscanearQR: () -> Unit,
    onVerCarrera: (String) -> Unit,
    onAbrirLobby: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.homeState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HeaderSection() }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
            
            if (state.activeSession != null) {
                item {
                    CarreraEnCursoCard(
                        session = state.activeSession!!,
                        onClick = { onAbrirLobby(state.activeSession!!.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
            
            item {
                RutasHeader({
                    Log.i("MyApp", "Crear ruta")
                    viewModel.updateMostrarCrearCarrera(true)
                })
            }

            if (state.isLoadingRaces) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9800))
                    }
                }
            } else {
                items(state.publicRaces.take(5)) { carrera ->
                    HomeCarreraCard(
                        carrera = carrera,
                        onClick = { onVerCarrera(carrera.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                InvitarAmigosCard(onInvitar = {
                    Log.i("MyApp", "Invitar Amigos clicked")
                    viewModel.updateMostrarNFC(true)
                })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .size(56.dp)
                .background(Color(0xFFFF9800), CircleShape)
                .clickable {
                    Log.i("MyApp", "Menu flotante clicked")
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
                    Log.i("MyApp", "Carrera creada desde Home")
                    viewModel.updateMostrarCrearCarrera(false)
                    viewModel.cargarDatos()
                }
            )
        }

        if (state.mostrarCrearPunto) {
            CrearPunto(
                onCerrar = { viewModel.updateMostrarCrearPunto(false) },
                onPublicar = {
                    Log.i("MyApp", "Punto publicado desde Home")
                    viewModel.updateMostrarCrearPunto(false)
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
fun CarreraEnCursoCard(
    session: RaceSession,
    onClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val checkpointsDone = currentUserId?.let { session.participants[it]?.checkpointsDone?.size } ?: 0
    val statusText = if (session.status == "active") "En curso" else "En lobby"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)),
        border = BorderStroke(2.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (session.status == "active") Icons.AutoMirrored.Filled.DirectionsRun else Icons.Default.Route,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.raceName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Participando: $statusText · $checkpointsDone puntos",
                    color = Color(0xFFFF9800),
                    fontSize = 13.sp
                )
            }
            
            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
        }
    }
}

@Composable
fun RutasHeader(onCrearRuta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Carreras destacadas",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Ver todas",
            color = Color(0xFFFF9800),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { /* Podría navegar a Carreras */ }
        )
    }
}

@Composable
fun HomeCarreraCard(
    carrera: Race,
    onClick: () -> Unit
) {
    val difficultyColor = when (carrera.difficulty.lowercase()) {
        "facil" -> Color(0xFF22C55E)
        "media" -> Color(0xFFEAB308)
        "dificil" -> Color(0xFFEF4444)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0x12FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x26FF9800)),
                contentAlignment = Alignment.Center
            ) {
                if (carrera.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = carrera.photoUrl,
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    carrera.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${"%.1f".format(carrera.estimatedDistanceKm)} km", color = Color.Gray, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(difficultyColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            carrera.difficulty.uppercase(),
                            color = difficultyColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun InvitarAmigosCard(onInvitar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.GroupAdd, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invitar Amigos", fontWeight = FontWeight.Bold)
            }
        }
    }
}