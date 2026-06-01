package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.Checkpoint
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearCarreraState(
    val nombre: String = "",
    val descripcion: String = "",
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val tipoOrden: String = "libre",
    val tiempoLimite: String = "sin-limite",
    val tipoCarrera: String = "rapida",
    val checkpoints: List<Checkpoint> = emptyList(),
    val mostrarSeleccionMapa: Boolean = false
) {
    val lugaresSeleccionados: Int get() = checkpoints.size
}

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

    fun setMostrarSeleccionMapa(mostrar: Boolean) {
        _crearCarreraState.update { it.copy(mostrarSeleccionMapa = mostrar) }
    }

    fun onPuntoSeleccionado(lat: Double, lng: Double) {
        _crearCarreraState.update { state ->
            val newCheckpoint = Checkpoint(
                name = "Checkpoint ${state.checkpoints.size + 1}",
                coordinates = GeoPoint(lat, lng),
                order = state.checkpoints.size + 1
            )
            state.copy(
                checkpoints = state.checkpoints + newCheckpoint,
                mostrarSeleccionMapa = false
            )
        }
    }

    fun removeCheckpoint(checkpoint: Checkpoint) {
        _crearCarreraState.update { state ->
            val newList = state.checkpoints.filter { it != checkpoint }
                .mapIndexed { index, cp -> cp.copy(order = index + 1) }
            state.copy(checkpoints = newList)
        }
    }

    fun onCerrarSeleccionMapa() {
        _crearCarreraState.update { it.copy(mostrarSeleccionMapa = false) }
    }
}
