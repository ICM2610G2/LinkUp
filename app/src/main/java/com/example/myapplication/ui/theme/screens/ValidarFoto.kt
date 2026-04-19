package com.example.myapplication.ui.theme.screens

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

import androidx.camera.core.Preview as CameraPreview
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ValidarFoto(
    nombreLugar: String,
    onCerrar: () -> Unit,
    onConfirmar: () -> Unit
) {
    var fotoTomada by remember { mutableStateOf(false) }
    var validando by remember { mutableStateOf(false) }
    var resultadoValidacion by remember { mutableStateOf<ResultadoValidacion?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!fotoTomada) {
            VistaCamera(
                onImageCaptureReady = { imageCapture = it }
            )
            MarcoEncuadre()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        HeaderValidacion(
            nombreLugar = nombreLugar,
            onCerrar = {
                Log.i("MyApp", "Cerrar validación clicked")
                onCerrar()
            }
        )

        if (resultadoValidacion != null) {
            IndicadoresValidacion(
                resultado = resultadoValidacion!!,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
            )
        }

        if (validando) {
            OverlayValidando()
        }

        ControlesInferiores(
            fotoTomada = fotoTomada,
            validando = validando,
            resultadoValidacion = resultadoValidacion,
            onTomarFoto = {
                Log.i("MyApp", "Tomar foto clicked: $nombreLugar")
                fotoTomada = true
                validando = true
                resultadoValidacion = null
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    validando = false
                    resultadoValidacion = ResultadoValidacion(
                        ubicacion = true,
                        movimiento = true,
                        orientacion = true
                    )
                }, 2000)
            },
            onConfirmar = {
                Log.i("MyApp", "Confirmar llegada clicked: $nombreLugar")
                onConfirmar()
                onCerrar()
            },
            onReintentar = {
                Log.i("MyApp", "Reintentar foto clicked")
                fotoTomada = false
                resultadoValidacion = null
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

data class ResultadoValidacion(
    val ubicacion: Boolean,
    val movimiento: Boolean,
    val orientacion: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VistaCamera(onImageCaptureReady: (ImageCapture) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCaptureInstance = ImageCapture.Builder().build()
                onImageCaptureReady(imageCaptureInstance)
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCaptureInstance
                    )
                } catch (e: Exception) {
                    Log.i("MyApp", "Error iniciando camara: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MarcoEncuadre() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(260.dp)) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .background(Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Transparent)
                    .drawBehind {
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 4f)
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 4f)
                    }
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .drawBehind {
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 4f)
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 4f)
                    }
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomStart)
                    .drawBehind {
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 4f)
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 4f)
                    }
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .drawBehind {
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 4f)
                        drawLine(Color(0xFFFF9800), androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 4f)
                    }
            )
        }
    }
}

@Composable
fun HeaderValidacion(nombreLugar: String, onCerrar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC000000), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                "Validar llegada",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Text(
                nombreLugar,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onCerrar,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .background(Color(0x1AFFFFFF), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun IndicadoresValidacion(resultado: ResultadoValidacion, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemValidacion(
                valido = resultado.ubicacion,
                titulo = "Ubicación GPS",
                subtitulo = "Coincide con el destino"
            )
            ItemValidacion(
                valido = resultado.movimiento,
                titulo = "Acelerómetro",
                subtitulo = "Movimiento detectado"
            )
            ItemValidacion(
                valido = resultado.orientacion,
                titulo = "Magnetómetro",
                subtitulo = "Orientación correcta"
            )
        }
    }
}

@Composable
fun ItemValidacion(valido: Boolean, titulo: String, subtitulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (valido) Color(0xFF22C55E) else Color(0xFFEF4444),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (valido) Icons.Default.Check else Icons.Default.Close,
                null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitulo, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
fun OverlayValidando() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Validando ubicación...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Verificando sensores del dispositivo",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ControlesInferiores(
    fotoTomada: Boolean,
    validando: Boolean,
    resultadoValidacion: ResultadoValidacion?,
    onTomarFoto: () -> Unit,
    onConfirmar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xCC000000))
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (!fotoTomada) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable { onTomarFoto() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color.Black, CircleShape)
                )
            }
        } else if (!validando && resultadoValidacion != null) {
            val todoValido = resultadoValidacion.ubicacion &&
                    resultadoValidacion.movimiento &&
                    resultadoValidacion.orientacion

            if (todoValido) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar llegada", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onReintentar,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tomar otra foto", color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onReintentar,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reintentar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}