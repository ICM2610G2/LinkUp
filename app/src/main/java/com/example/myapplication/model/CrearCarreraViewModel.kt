package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearCarreraState(
    val nombre: String = "",
    val descripcion: String = "",
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CrearCarreraViewModel : ViewModel() {
    private val _crearCarreraState = MutableStateFlow(CrearCarreraState())
    val crearCarreraState = _crearCarreraState.asStateFlow()

    fun updateNombre(newValue: String) {
        _crearCarreraState.update { it.copy(nombre = newValue) }
    }
    fun updateDescripcion(newValue: String) {
        _crearCarreraState.update { it.copy(descripcion = newValue) }
    }
    fun updateIsPublic(newValue: Boolean) {
        _crearCarreraState.update { it.copy(isPublic = newValue) }
    }
    fun updateIsLoading(newValue: Boolean) {
        _crearCarreraState.update { it.copy(isLoading = newValue) }
    }
    fun updateErrorMessage(newValue: String?) {
        _crearCarreraState.update { it.copy(errorMessage = newValue) }
    }
}