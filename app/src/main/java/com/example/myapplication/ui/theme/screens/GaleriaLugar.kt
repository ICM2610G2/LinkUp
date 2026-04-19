package com.example.myapplication.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FotoGaleria(
    val id: Int,
    val usuario: String,
    val iniciales: String,
    val likes: Int,
    val imageRes: Int? = null
)

@Composable
fun GaleriaLugar(
    nombreLugar: String,
    onCerrar: () -> Unit
) {
    val fotos = remember {
        listOf(
            FotoGaleria(1, "Carlos Ruiz", "CR", 24),
            FotoGaleria(2, "Ana Martínez", "AM", 18),
            FotoGaleria(3, "Luis Pérez", "LP", 31),
            FotoGaleria(4, "María González", "MG", 15),
            FotoGaleria(5, "Carlos Ruiz", "CR", 22),
            FotoGaleria(6, "Sofia Ramírez", "SR", 28)
        )
    }

    var fotosConLike by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
    ) {
        HeaderGaleria(
            nombreLugar = nombreLugar,
            totalFotos = fotos.size,
            onCerrar = {
                Log.i("MyApp", "Cerrar galeria clicked")
                onCerrar()
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            items(fotos) { foto ->
                ItemFotoGaleria(
                    foto = foto,
                    tieneLike = fotosConLike.contains(foto.id),
                    onToggleLike = {
                        fotosConLike = if (fotosConLike.contains(foto.id)) {
                            Log.i("MyApp", "Unlike foto: ${foto.id} de ${foto.usuario}")
                            fotosConLike - foto.id
                        } else {
                            Log.i("MyApp", "Like foto: ${foto.id} de ${foto.usuario}")
                            fotosConLike + foto.id
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderGaleria(
    nombreLugar: String,
    totalFotos: Int,
    onCerrar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF21A1A1A))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                nombreLugar,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Place,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    "$totalFotos fotos de exploradores",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

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
}

@Composable
fun ItemFotoGaleria(
    foto: FotoGaleria,
    tieneLike: Boolean,
    onToggleLike: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0DFFFFFF))
    ) {
        Column {
            Box(modifier = Modifier.aspectRatio(1f)) {
                if (foto.imageRes != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = foto.imageRes),
                        contentDescription = "Foto de ${foto.usuario}",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF252525)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color(0x80000000), CircleShape)
                        .clickable { onToggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = if (tieneLike) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF2A9D8F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        foto.iniciales,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    foto.usuario,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        (foto.likes + if (tieneLike) 1 else 0).toString(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}