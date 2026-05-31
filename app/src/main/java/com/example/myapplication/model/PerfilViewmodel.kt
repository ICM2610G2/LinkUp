package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilState(
    val showDeleteDialog: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val deletePassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showBiometricConfirm: Boolean = false
)

class PerfilViewModel : ViewModel() {
    private val _perfilState = MutableStateFlow(PerfilState())
    val perfilState = _perfilState.asStateFlow()

    fun updateShowDeleteDialog(newValue: Boolean) {
        _perfilState.update { it.copy(showDeleteDialog = newValue) }
    }
    fun updateShowPasswordDialog(newValue: Boolean) {
        _perfilState.update { it.copy(showPasswordDialog = newValue) }
    }
    fun updateDeletePassword(newValue: String) {
        _perfilState.update { it.copy(deletePassword = newValue) }
    }
    fun updateIsLoading(newValue: Boolean) {
        _perfilState.update { it.copy(isLoading = newValue) }
    }
    fun updateErrorMessage(newValue: String?) {
        _perfilState.update { it.copy(errorMessage = newValue) }
    }
    fun updateShowBiometricConfirm(newValue: Boolean) {
        _perfilState.update { it.copy(showBiometricConfirm = newValue) }
    }
}