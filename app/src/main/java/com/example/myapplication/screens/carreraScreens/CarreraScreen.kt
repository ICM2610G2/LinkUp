package com.example.myapplication.screens.carreraScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.models.Race
import com.example.myapplication.model.InvitarAmigosViewModel
import com.example.myapplication.repository.RaceRepository
import kotlinx.coroutines.launch

@Composable
fun CarreraScreen(
    raceId: String,
    onBack: () -> Unit,
    onAbrirLobby: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }
    var race by remember { mutableStateOf<Race?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isCreatingSession by remember { mutableStateOf(false) }

    var mostrarInvitar by remember { mutableStateOf(false) }
    val invitarViewModel: InvitarAmigosViewModel = viewModel()
    val invitarState by invitarViewModel.state.collectAsState()

    LaunchedEffect(invitarState.enviado) {
        if (invitarState.enviado) {
            mostrarInvitar = false
            invitarViewModel.resetEnviado()
        }
    }

    LaunchedEffect(raceId) {
        race = raceRepository.getRaceById(raceId)
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFF9800)
            )
        } else if (race != null) {
            val raceData = race!!
            Column(modifier = Modifier.fillMaxSize()) {

                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    if (raceData.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = raceData.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Place,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF0A0A0A).copy(alpha = 0.8f)),
                                    startY = 300f
                                )
                            )
                    )

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        raceData.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfoChip(
                            text = "${"%.1f".format(raceData.estimatedDistanceKm)} km",
                            icon = Icons.Default.Place
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        InfoChip(
                            text = "${raceData.checkpointCount} puntos",
                            icon = Icons.Default.EmojiEvents
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        val difficultyColor = when (raceData.difficulty) {
                            "facil" -> Color(0xFF22C55E)
                            "media" -> Color(0xFFEAB308)
                            "dificil" -> Color(0xFFEF4444)
                            else -> Color(0xFFFF9800)
                        }

                        Box(
                            modifier = Modifier
                                .background(difficultyColor, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                raceData.difficulty.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Descripción",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        raceData.description,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = {
                            invitarViewModel.cargarAmigos()
                            mostrarInvitar = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9800)),
                        border = BorderStroke(1.dp, Color(0xFFFF9800)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Invitar amigos",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isCreatingSession = true
                                val result = raceRepository.findOrJoinLobby(raceData)
                                result.onSuccess { sessionId ->
                                    onAbrirLobby(sessionId)
                                }
                                isCreatingSession = false
                            }
                        },
                        enabled = !isCreatingSession,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isCreatingSession) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Iniciar Sesión / Unirse",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarInvitar) {
        AlertDialog(
            onDismissRequest = { mostrarInvitar = false },
            containerColor = Color(0xFF1A1A1A),
            title = {
                Text("Invitar amigos", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Box(modifier = Modifier.heightIn(min = 80.dp, max = 400.dp)) {
                    when {
                        invitarState.isLoading -> {
                            CircularProgressIndicator(
                                color = Color(0xFFFF9800),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        invitarState.amigos.isEmpty() -> {
                            Text("No tienes amigos agregados aún.", color = Color.Gray)
                        }
                        else -> {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                invitarState.amigos.forEach { amigo ->
                                    val seleccionado = invitarState.seleccionados.contains(amigo.uid)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { invitarViewModel.toggleSeleccion(amigo.uid) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = seleccionado,
                                            onCheckedChange = {
                                                invitarViewModel.toggleSeleccion(amigo.uid)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFF9800)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                amigo.displayName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                amigo.gameId,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        race?.let { invitarViewModel.enviarInvitaciones(it.name, it.id) }
                    },
                    enabled = invitarState.seleccionados.isNotEmpty() && !invitarState.isEnviando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (invitarState.isEnviando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Invitar (${invitarState.seleccionados.size})")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarInvitar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}