package com.example.myapplication.ui.theme.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import com.example.myapplication.ui.theme.model.GpsPoint

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Mapa() {
    var mostrarValidarFoto by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        MapaFondo()
        MarcadorDestino(
            modifier = Modifier.align(Alignment.Center).offset(y = (-60).dp)
        )
        MarcadorUsuario(
            modifier = Modifier.align(Alignment.Center).offset(y = 80.dp)
        )
        PuntosVisitados()
        DestinoCard(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, end = 80.dp, top = 60.dp)
        )
        BotonesLaterales(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 16.dp)
        )
        CardInferior(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
            onValidarFoto = {
                Log.i("MyApp", "Validar foto")
                mostrarValidarFoto = true }
        )
        if (mostrarValidarFoto) {
            ValidarFoto(
                nombreLugar = "Plaza de Bolívar",
                onCerrar = { mostrarValidarFoto = false },
                onConfirmar = {
                    Log.i("MyApp", "Llegada confirmada: Plaza de Bolívar")
                    mostrarValidarFoto = false
                }
            )
        }
    }
}

@Composable
fun MapaFondo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                dibujarMapa(this)
            }
    )
}

fun dibujarMapa(scope: DrawScope) {
    val naranja = Color(0xFFFF9800)
    val naranjaClaro = Color(0xFFFFA500)
    val gridSize = 80.dp.value * scope.density

    // Grid de fondo
    var x = 0f
    while (x < scope.size.width) {
        scope.drawLine(
            color = naranja.copy(alpha = 0.15f),
            start = Offset(x, 0f),
            end = Offset(x, scope.size.height),
            strokeWidth = 1f
        )
        x += gridSize
    }
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(
            color = naranja.copy(alpha = 0.15f),
            start = Offset(0f, y),
            end = Offset(scope.size.width, y),
            strokeWidth = 1f
        )
        y += gridSize
    }

    // Calles principales verticales
    scope.drawLine(naranjaClaro.copy(alpha = 0.6f), Offset(scope.size.width * 0.5f, 0f), Offset(scope.size.width * 0.5f, scope.size.height), strokeWidth = 3f)
    scope.drawLine(naranjaClaro.copy(alpha = 0.4f), Offset(scope.size.width * 0.3f, 0f), Offset(scope.size.width * 0.3f, scope.size.height), strokeWidth = 3f)
    scope.drawLine(naranjaClaro.copy(alpha = 0.4f), Offset(scope.size.width * 0.7f, 0f), Offset(scope.size.width * 0.7f, scope.size.height), strokeWidth = 3f)

    // Calles principales horizontales
    scope.drawLine(naranjaClaro.copy(alpha = 0.6f), Offset(0f, scope.size.height * 0.5f), Offset(scope.size.width, scope.size.height * 0.5f), strokeWidth = 3f)
    scope.drawLine(naranjaClaro.copy(alpha = 0.4f), Offset(0f, scope.size.height * 0.3f), Offset(scope.size.width, scope.size.height * 0.3f), strokeWidth = 3f)
    scope.drawLine(naranjaClaro.copy(alpha = 0.4f), Offset(0f, scope.size.height * 0.7f), Offset(scope.size.width, scope.size.height * 0.7f), strokeWidth = 3f)

    // Círculos de la plaza
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.35f
    scope.drawCircle(naranja.copy(alpha = 0.5f), radius = 60.dp.value * scope.density, center = Offset(cx, cy), style = Stroke(width = 2f))
    scope.drawCircle(naranja.copy(alpha = 0.3f), radius = 40.dp.value * scope.density, center = Offset(cx, cy), style = Stroke(width = 1f))
}
@Composable
fun MarcadorDestino(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFFF9800), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFA500), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Layers, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("!", color = Color(0xFFFF9800), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarcadorUsuario(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x4D22C55E), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF22C55E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Navigation, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}
@Composable
fun PuntosVisitados() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 120.dp, top = 220.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF16A34A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 200.dp, top = 120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF16A34A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 220.dp, top = 260.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF6B7280), CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 140.dp, top = 100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF6B7280), CircleShape)
            )
        }
    }
}

@Composable
fun DestinoCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "DESTINO",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Plaza de Bolívar",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFF9800), CircleShape)
                    )
                    Text("350m", color = Color(0xFFFF9800), fontSize = 12.sp)
                }
                Text("~ 4 min", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BotonesLaterales(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF22C55E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }
                Text("Caminando", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("Validado ✓", color = Color(0xFF22C55E), fontSize = 8.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clickable { Log.i("MyApp", "Brújula clicked") },
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(Icons.Default.Explore, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clickable { Log.i("MyApp", "Capas clicked") },
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(Icons.Default.Layers, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CardInferior(modifier: Modifier = Modifier, onValidarFoto: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Brújula", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("N 23° NE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                VisualizacionBrujula()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Velocidad", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("5.2 km/h", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Movimiento", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Activo ✓", color = Color(0xFF22C55E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onValidarFoto,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validar llegada con foto", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun VisualizacionBrujula() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .drawBehind {
                val cx = size.width / 2
                val cy = size.height / 2
                drawCircle(
                    color = Color(0x4DFF9800),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = size.minDimension / 2 - 8.dp.toPx(),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = Color(0xFFFF9800),
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - 18.dp.toPx()),
                    strokeWidth = 3f
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text("N", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}


