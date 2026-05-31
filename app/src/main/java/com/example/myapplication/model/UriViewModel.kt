package com.example.myapplication.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CameraState(
    val imageUri: Uri? = null
)

class CameraViewModel : ViewModel() {
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState = _cameraState.asStateFlow()

    fun updateImageUri(newValue: Uri?) {
        _cameraState.update { it.copy(imageUri = newValue) }
    }
}