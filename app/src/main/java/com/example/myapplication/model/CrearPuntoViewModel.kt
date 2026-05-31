package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearPuntoState(
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "cultural",
    val dificultad: String = "facil",
    val obteniendo: Boolean = false,
    val ubicacion: Pair<Double, Double>? = null
)

class CrearPuntoViewModel : ViewModel() {
    private val _crearPuntoState = MutableStateFlow(CrearPuntoState())
    val crearPuntoState = _crearPuntoState.asStateFlow()

    fun updateNombre(newValue: String) {
        _crearPuntoState.update { it.copy(nombre = newValue) }
    }
    fun updateDescripcion(newValue: String) {
        _crearPuntoState.update { it.copy(descripcion = newValue) }
    }
    fun updateCategoria(newValue: String) {
        _crearPuntoState.update { it.copy(categoria = newValue) }
    }
    fun updateDificultad(newValue: String) {
        _crearPuntoState.update { it.copy(dificultad = newValue) }
    }
    fun updateObteniendo(newValue: Boolean) {
        _crearPuntoState.update { it.copy(obteniendo = newValue) }
    }
    fun updateUbicacion(newValue: Pair<Double, Double>?) {
        _crearPuntoState.update { it.copy(ubicacion = newValue) }
    }
}