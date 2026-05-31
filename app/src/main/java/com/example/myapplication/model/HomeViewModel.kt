package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeState(
    val mostrarCrearPunto: Boolean = false,
    val mostrarMenuFlotante: Boolean = false,
    val mostrarCrearCarrera: Boolean = false,
    val mostrarNFC: Boolean = false,
    val mostrarAmigos: Boolean = false,
    val lugarSeleccionado: String? = null
)

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    fun updateMostrarCrearPunto(newValue: Boolean) {
        _homeState.update { it.copy(mostrarCrearPunto = newValue) }
    }
    fun updateMostrarMenuFlotante(newValue: Boolean) {
        _homeState.update { it.copy(mostrarMenuFlotante = newValue) }
    }
    fun updateMostrarCrearCarrera(newValue: Boolean) {
        _homeState.update { it.copy(mostrarCrearCarrera = newValue) }
    }
    fun updateMostrarNFC(newValue: Boolean) {
        _homeState.update { it.copy(mostrarNFC = newValue) }
    }
    fun updateMostrarAmigos(newValue: Boolean) {
        _homeState.update { it.copy(mostrarAmigos = newValue) }
    }
    fun updateLugarSeleccionado(newValue: String?) {
        _homeState.update { it.copy(lugarSeleccionado = newValue) }
    }
}