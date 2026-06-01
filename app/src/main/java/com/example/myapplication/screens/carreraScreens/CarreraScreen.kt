package com.example.myapplication.screens.carreraScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.models.Race
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
                // Header Image & Back Button
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
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
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

                    // Gradient overlay
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
                        modifier = Modifier.padding(16.dp).background(Color.Black.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape)
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

                    Button(
                        onClick = {
                            scope.launch {
                                isCreatingSession = true
                                // Try to find an existing session first, or create a new one
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
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión / Unirse", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
