package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val email: String = "",
    val password: String = "",
    val esRegistro: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = ""
)

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun updateEmail(newValue: String) {
        _loginState.update { it.copy(email = newValue) }
    }
    fun updatePassword(newValue: String) {
        _loginState.update { it.copy(password = newValue) }
    }
    fun updateEsRegistro(newValue: Boolean) {
        _loginState.update { it.copy(esRegistro = newValue) }
    }
    fun updatePasswordVisible(newValue: Boolean) {
        _loginState.update { it.copy(passwordVisible = newValue) }
    }
    fun updateIsLoading(newValue: Boolean) {
        _loginState.update { it.copy(isLoading = newValue) }
    }
    fun updateErrorMessage(newValue: String?) {
        _loginState.update { it.copy(errorMessage = newValue) }
    }
    fun updateName(newValue: String) {
        _loginState.update { it.copy(name = newValue) }
    }
}