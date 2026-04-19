package com.example.myapplication.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp

@Composable
fun Perfil() {

    val accent = Color(0xFFFF9800)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
            .navigationBarsPadding() // respeta la barra inferior
            .padding(16.dp)
    )
   {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(accent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Imagen de perfil",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Nombre de Usuario",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Correo Electrónico",
                    fontSize = 14.sp,
                    color = Color(0xFFBDBDBD)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ID: 8414852",
                    fontSize = 14.sp,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        MenuItem(
            icon = Icons.Default.Settings,
            title = "Configuración",
            accent = accent
        )

        MenuItem(
            icon = Icons.Default.Timeline,
            title = "Historial de carreras",
            subtitle = "Ver todas tus carreras",
            accent = accent
        )

        MenuItem(
            icon = Icons.Default.CameraAlt,
            title = "Fotos guardadas",
            subtitle = "Revisa todos tus recuerdos",
            accent = accent
        )

        MenuItem(
            icon = Icons.Default.LocationOn,
            title = "Sensores del dispositivo",
            subtitle = "Acelerómetro, GPS, Magnetómetro",
            accent = accent
        )

        Spacer(modifier = Modifier.weight(1f))

       Card(
           modifier = Modifier
               .fillMaxWidth()
               .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 90.dp),
           shape = RoundedCornerShape(16.dp),
           colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0A0A))
       ) {
           Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(16.dp),
               horizontalArrangement = Arrangement.Center,
               verticalAlignment = Alignment.CenterVertically
           ) {
               Text(
                   text = "Cerrar sesión",
                   color = Color(0xFFFF6B6B),
                   fontSize = 16.sp,
                   fontWeight = FontWeight.Bold
               )
           }
       }
    }
}


@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    accent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Círculo del ícono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape)
                    .border(1.dp, accent.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Textos
            Column(
                modifier = Modifier.weight(1f) // ocupa el espacio del centro
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "›",
                color = Color(0xFF7A7A7A),
                fontSize = 22.sp
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