package com.example.myapplication.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.repository.FriendInviteRepository
import com.example.myapplication.utils.InviteUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@kotlin.OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Escanea un código QR", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
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
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = ContextCompat.getMainExecutor(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val analysisUseCase = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isProcessing) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (isProcessing) return@addOnSuccessListener
                                            
                                            for (barcode in barcodes) {
                                                val raw = barcode.rawValue
                                                if (raw != null) {
                                                    Log.d("QR_SCAN", "Contenido QR: $raw")
                                                    val uid = InviteUtils.extractUidFromLink(raw)
                                                    Log.d("QR_SCAN", "UID extraído: $uid")
                                                    
                                                    if (uid != null) {
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
                                                        break
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

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Se requiere permiso de cámara", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Conceder permiso")
                    }
                }
            }
        }
    }
}
