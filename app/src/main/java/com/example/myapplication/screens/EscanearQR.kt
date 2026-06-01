package com.example.myapplication.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.repository.FriendInviteRepository
import com.example.myapplication.utils.InviteUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EscanearQR(
    onBack: () -> Unit,
    onInviteSent: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val inviteRepository = remember { FriendInviteRepository() }
    
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Escanea un código QR", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (cameraPermissionState.status.isGranted) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        val executor = ContextCompat.getMainExecutor(context)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val scanner = BarcodeScanning.getClient()
                            val analysisUseCase = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isProcessing) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue
                                                if (rawValue != null && rawValue.startsWith("https://linkup.app/addfriend")) {
                                                    val uid = InviteUtils.extractUidFromLink(rawValue)
                                                    if (uid != null && !isProcessing) {
                                                        isProcessing = true
                                                        scope.launch {
                                                            val result = inviteRepository.sendInviteByUid(uid)
                                                            result.fold(
                                                                onSuccess = {
                                                                    Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show()
                                                                    onInviteSent()
                                                                },
                                                                onFailure = { e ->
                                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                                    isProcessing = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysisUseCase
                                )
                            } catch (exc: Exception) {
                                Log.e("EscanearQR", "Use case binding failed", exc)
                            }
                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay simple para guiar al usuario
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Aquí podrías dibujar un recuadro o visor
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Se requiere permiso de cámara para escanear", color = Color.White)
            }
        }
    }
}
