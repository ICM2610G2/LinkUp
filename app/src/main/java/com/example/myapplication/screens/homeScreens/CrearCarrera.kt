package com.example.myapplication.screens.homeScreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.model.CrearCarreraViewModel
import com.example.myapplication.repository.RaceRepository
import kotlinx.coroutines.launch

@Composable
fun CrearCarrera(
    onCerrar: () -> Unit,
    onCarreraCreada: () -> Unit,
    viewModel: CrearCarreraViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val raceRepository = remember { RaceRepository() }
    val state by viewModel.crearCarreraState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateImageUri(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HeaderCrearCarreraReal(onCerrar = onCerrar)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column {
                            Text(
                                "Foto de la carrera",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF252525))
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.imageUri != null) {
                                    AsyncImage(
                                        model = state.imageUri,
                                        contentDescription = "Previsualización",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateImageUri(null) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.AddAPhoto,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Subir foto", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        CampoTextoCarrera(
                            titulo = "Nombre de la carrera",
                            valor = state.nombre,
                            placeholder = "Ej: Ruta La Candelaria",
                            onChange = { viewModel.updateNombre(it) }
                        )
                    }

                    item {
                        CampoTextoCarrera(
                            titulo = "Descripción",
                            valor = state.descripcion,
                            placeholder = "Describe la ruta y el objetivo de la carrera",
                            onChange = { viewModel.updateDescripcion(it) },
                            multilinea = true
                        )
                    }

                    item {
                        Text(
                            "Visibilidad",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OpcionVisibilidad(
                                titulo = "Pública",
                                activa = state.isPublic,
                                icon = Icons.Default.Public,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateIsPublic(true) }
                            )
                            OpcionVisibilidad(
                                titulo = "Privada",
                                activa = !state.isPublic,
                                icon = Icons.Default.Lock,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateIsPublic(false) }
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Place,
                                            null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Checkpoints",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.setMostrarSeleccionMapa(true) },
                                        modifier = Modifier.size(32.dp).background(Color(0xFFFF9800), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (state.checkpoints.isEmpty()) {
                                    Text(
                                        "No hay checkpoints agregados.",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    state.checkpoints.forEach { checkpoint ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "${checkpoint.order}. ${checkpoint.name}",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 13.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { viewModel.removeCheckpoint(checkpoint) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = Color(0xFFFF6B6B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.errorMessage != null) {
                        item {
                            Text(
                                state.errorMessage!!,
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.updateIsLoading(true)
                            viewModel.updateErrorMessage(null)

                            val result = raceRepository.createRace(
                                name = state.nombre,
                                description = state.descripcion,
                                isPublic = state.isPublic,
                                checkpoints = state.checkpoints,
                                imageUri = state.imageUri
                            )

                            result.fold(
                                onSuccess = {
                                    viewModel.updateIsLoading(false)
                                    onCarreraCreada()
                                },
                                onFailure = { e ->
                                    viewModel.updateIsLoading(false)
                                    viewModel.updateErrorMessage(
                                        e.message ?: "No se pudo crear la carrera"
                                    )
                                }
                            )
                        }
                    },
                    enabled = state.nombre.isNotBlank() && state.descripcion.isNotBlank() && state.checkpoints.isNotEmpty() && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Crear carrera",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (state.mostrarSeleccionMapa) {
            Box(modifier = Modifier.fillMaxSize()) {
                SeleccionMapa(
                    onPuntoSeleccionado = { lat, lng ->
                        viewModel.onPuntoSeleccionado(lat, lng)
                    },
                    onCerrar = {
                        viewModel.setMostrarSeleccionMapa(false)
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderCrearCarreraReal(onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Crear carrera",
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
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun CampoTextoCarrera(
    titulo: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit,
    multilinea: Boolean = false
) {
    Column {
        Text(
            titulo,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (multilinea) 110.dp else 56.dp),
            placeholder = {
                Text(placeholder, color = Color.White.copy(alpha = 0.35f))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF9800),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = !multilinea,
            maxLines = if (multilinea) 4 else 1
        )
    }
}

@Composable
fun OpcionVisibilidad(
    titulo: String,
    activa: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activa) Color(0xFFFF9800) else Color(0xFF252525)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (activa) Color.White else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                titulo,
                color = if (activa) Color.White else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}