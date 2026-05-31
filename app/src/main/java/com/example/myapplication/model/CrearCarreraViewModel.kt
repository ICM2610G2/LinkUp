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
    val puntos: List<Pair<Double, Double>> = emptyList(),
    val mostrarSeleccionMapa: Boolean = false
) {
    val lugaresSeleccionados: Int get() = puntos.size
}

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
        _crearCarreraState.update { it.copy(mostrarSeleccionMapa = true) }
    }

    fun onPuntoSeleccionado(lat: Double, lng: Double) {
        _crearCarreraState.update { state ->
            state.copy(
                puntos = state.puntos + (lat to lng),
                mostrarSeleccionMapa = false
            )
        }
    }

    fun onCerrarSeleccionMapa() {
        _crearCarreraState.update { it.copy(mostrarSeleccionMapa = false) }
    }
}
