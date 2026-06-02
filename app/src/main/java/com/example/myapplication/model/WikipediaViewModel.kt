package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.WikipediaPlace
import com.example.myapplication.repository.WikipediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WikipediaState(
    val isLoading: Boolean = false,
    val place: WikipediaPlace? = null,
    val errorMessage: String? = null
)

class WikipediaViewModel : ViewModel() {

    private val repository = WikipediaRepository()

    private val _state = MutableStateFlow(WikipediaState())
    val state = _state.asStateFlow()

    fun buscarLugar(nombreLugar: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    place = null,
                    errorMessage = null
                )
            }

            val result = repository.getPlaceInfo(nombreLugar)

            result.fold(
                onSuccess = { place ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            place = place,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            place = null,
                            errorMessage = error.message ?: "Error consultando Wikipedia"
                        )
                    }
                }
            )
        }
    }

    fun limpiar() {
        _state.update {
            WikipediaState()
        }
    }
}