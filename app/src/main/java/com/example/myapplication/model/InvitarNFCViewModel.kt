package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InvitarNFCState(
    val mostrarQR: Boolean = false,
    val escaneando: Boolean = false
)

class InvitarNFCViewModel : ViewModel() {
    private val _state = MutableStateFlow(InvitarNFCState())
    val state = _state.asStateFlow()

    fun updateMostrarQR(newValue: Boolean) {
        _state.update { it.copy(mostrarQR = newValue) }
    }

    fun updateEscaneando(newValue: Boolean) {
        _state.update { it.copy(escaneando = newValue) }
    }
}