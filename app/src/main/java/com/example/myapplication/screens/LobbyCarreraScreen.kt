package com.example.myapplication.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.LobbyViewModel
import com.example.myapplication.utils.showRaceStartNotification
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LobbyCarreraScreen(
    sessionId: String,
    onBack: () -> Unit,
    onRaceStarted: (String) -> Unit,
    lobbyViewModel: LobbyViewModel = viewModel()
) {
    val state by lobbyViewModel.state.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Arrancar el listener cuando entra a la pantalla
    LaunchedEffect(sessionId) {
        lobbyViewModel.startListening(sessionId)
    }

    // Reaccionar cuando la carrera arranca
    LaunchedEffect(state.raceStarted) {
        if (state.raceStarted) {
            showRaceStartNotification(context, state.raceName)
            delay(500)
            lobbyViewModel.clearRaceStarted()
            onRaceStarted(sessionId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Lobby de carrera", color = Color(0xFFFF9800), fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Text(state.raceName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Groups, null, tint = Color(0xFFFF9800), modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Participantes", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${state.participantsCount} jugador(es) en el lobby", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Estado: ${state.status}", color = Color.Gray)

            if (state.isParticipant) {
                Text("Ya eres parte de esta carrera", color = Color(0xFF22C55E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            } else if (state.status == "lobby") {
                Text("Aún no te has unido a esta carrera", color = Color.Gray, fontSize = 13.sp)
            }

            if (currentUid != state.createdBy && state.isParticipant && state.status == "lobby") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Esperando que el creador inicie la carrera", color = Color.Gray, fontSize = 12.sp)
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.errorMessage!!, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!state.isParticipant && state.status == "lobby") {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        lobbyViewModel.joinRace(sessionId)
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unirse a la carrera", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (currentUid == state.createdBy) {
                Button(
                    onClick = { lobbyViewModel.startRace(sessionId) },
                    enabled = state.participantsCount >= 2 && state.status == "lobby" && !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar carrera", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}