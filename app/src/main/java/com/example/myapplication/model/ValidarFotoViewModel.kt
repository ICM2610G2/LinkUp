package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import com.example.myapplication.screens.ResultadoValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ValidarFotoState(
    val fotoTomada: Boolean = false,
    val validando: Boolean = false,
    val resultadoValidacion: ResultadoValidacion? = null
)

class ValidarFotoViewModel : ViewModel() {
    private val _state = MutableStateFlow(ValidarFotoState())
    val state = _state.asStateFlow()

    fun updateFotoTomada(newValue: Boolean) {
        _state.update { it.copy(fotoTomada = newValue) }
    }

    fun updateValidando(newValue: Boolean) {
        _state.update { it.copy(validando = newValue) }
    }

    fun updateResultadoValidacion(newValue: ResultadoValidacion?) {
        _state.update { it.copy(resultadoValidacion = newValue) }
    }

    fun tomarFoto(nombreLugar: String) {
        updateFotoTomada(true)
        updateValidando(true)
        updateResultadoValidacion(null)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            updateValidando(false)
            updateResultadoValidacion(
                ResultadoValidacion(
                    ubicacion = true,
                    movimiento = true,
                    orientacion = true
                )
            )
        }, 2000)
    }

    fun reintentar() {
        updateFotoTomada(false)
        updateResultadoValidacion(null)
    }
}