package com.example.myapplication.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditProfileState(
    val displayName: String = "",
    val photoURL: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedImageUri: Uri? = null
)

class EditProfileViewModel : ViewModel() {
    private val _editProfileState = MutableStateFlow(EditProfileState())
    val editProfileState = _editProfileState.asStateFlow()

    fun updateDisplayName(newValue: String) {
        _editProfileState.update { it.copy(displayName = newValue) }
    }
    fun updatePhotoURL(newValue: String) {
        _editProfileState.update { it.copy(photoURL = newValue) }
    }
    fun updateIsLoading(newValue: Boolean) {
        _editProfileState.update { it.copy(isLoading = newValue) }
    }
    fun updateErrorMessage(newValue: String?) {
        _editProfileState.update { it.copy(errorMessage = newValue) }
    }
    fun updateSelectedImageUri(newValue: Uri?) {
        _editProfileState.update { it.copy(selectedImageUri = newValue) }
    }

    // Para inicializar el estado con los datos del usuario
    fun initWithUser(displayName: String, photoURL: String) {
        _editProfileState.update {
            it.copy(displayName = displayName, photoURL = photoURL)
        }
    }
}