package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearCarreraState(
    val nombre: String = "",
    val tipoOrden: String = "libre",
    val tiempoLimite: String = "sin-limite",
    val tipoCarrera: String = "rapida",
    val lugaresSeleccionados: Int = 0
)

class CrearCarreraViewModel : ViewModel() {
    private val _crearCarreraState = MutableStateFlow(CrearCarreraState())
    val crearCarreraState = _crearCarreraState.asStateFlow()

    fun updateNombre(newValue: String) {
        _crearCarreraState.update { it.copy(nombre = newValue) }
    }
    fun updateTipoOrden(newValue: String) {
        _crearCarreraState.update { it.copy(tipoOrden = newValue) }
    }
    fun updateTiempoLimite(newValue: String) {
        _crearCarreraState.update { it.copy(tiempoLimite = newValue) }
    }
    fun updateTipoCarrera(newValue: String) {
        _crearCarreraState.update { it.copy(tipoCarrera = newValue) }
    }
    fun agregarLugar() {
        _crearCarreraState.update { it.copy(lugaresSeleccionados = minOf(it.lugaresSeleccionados + 1, 10)) }
    }
}