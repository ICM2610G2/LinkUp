package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp

@Composable
fun Perfil() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        )
    ) {
        item { PerfilHeader() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { StatsGridSection() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { NivelXpSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { InsigniasSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { MenuSection() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { CerrarSesionButton() }
    }
}

@Composable
fun PerfilHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFFF9800), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "JD",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Juan David",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "@juandavid · Bogotá, Colombia",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF9800), CircleShape)
                        )
                        Text(
                            "Nivel 12",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    "Explorador",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun StatsGridSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatGridItem("24", "Carreras", Icons.Default.EmojiEvents, modifier = Modifier.weight(1f))
        StatGridItem("87", "Fotos", Icons.Default.CameraAlt, modifier = Modifier.weight(1f))
        StatGridItem("42", "Lugares", Icons.Default.Place, modifier = Modifier.weight(1f))
        StatGridItem("7", "Racha", Icons.Default.Bolt, modifier = Modifier.weight(1f))
    }
}
@Composable
fun StatGridItem(valor: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0DFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(valor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
fun NivelXpSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0DFFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Nivel 12",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "2,450 / 3,000 XP",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.82f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFFFF9800),
                trackColor = Color(0xFF252525),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "550 XP para el siguiente nivel",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun InsigniasSection() {
    val insignias = listOf(
        Pair("🗺️", "Explorador") to true,
        Pair("⚡", "Velocista") to true,
        Pair("📷", "Fotógrafo") to true,
        Pair("🌙", "Nocturno") to false,
        Pair("👑", "Leyenda") to false
    )

    Column {
        Text(
            "Insignias",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            insignias.forEach { (info, obtenida) ->
                InsigniaItem(
                    emoji = info.first,
                    nombre = info.second,
                    obtenida = obtenida
                )
            }
        }
    }
}

@Composable
fun InsigniaItem(emoji: String, nombre: String, obtenida: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (obtenida) Color(0xFF1A1A1A) else Color(0x661A1A1A)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(64.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (obtenida) Color(0x4DFF9800) else Color(0x0DFFFFFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                emoji,
                fontSize = 24.sp,
                color = if (obtenida) Color.White else Color.White.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                nombre,
                color = if (obtenida) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MenuSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MenuItem(Icons.Default.EmojiEvents, "Historial de carreras", null)
        MenuItem(Icons.Default.Photo, "Fotos guardadas", null)
        MenuItem(Icons.Default.Smartphone, "Sensores del dispositivo", "Acelerómetro, GPS, Magnetómetro")
        MenuItem(Icons.Default.Settings, "Configuración", null)
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    titulo: String,
    subtitulo: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Log.i("MyApp", "$titulo clicked") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0x0DFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF252525), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f) // ocupa el espacio del centro
            ) {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (subtitulo != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitulo,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CerrarSesionButton() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFF3B30)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Log.i("MyApp", "Cerrar sesión clicked") },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FF3B30))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Cerrar sesión",
                color = Color(0xFFFF3B30),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    MyApplicationTheme {
        Perfil()
    }
}