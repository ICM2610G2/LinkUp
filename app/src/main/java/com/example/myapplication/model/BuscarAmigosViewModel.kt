package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BuscarAmigosState(
    val searchQuery: String = "",
    val searchResult: User? = null,
    val isLoadingUser: Boolean = true,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val friendStatus: String? = null,
    val isSending: Boolean = false,
    val currentUser: User? = null
)

class BuscarAmigosViewModel : ViewModel() {
    private val _buscarAmigosState = MutableStateFlow(BuscarAmigosState())
    val buscarAmigosState = _buscarAmigosState.asStateFlow()

    fun updateSearchQuery(newValue: String) {
        _buscarAmigosState.update { it.copy(searchQuery = newValue) }
    }
    fun updateSearchResult(newValue: User?) {
        _buscarAmigosState.update { it.copy(searchResult = newValue) }
    }
    fun updateIsLoadingUser(newValue: Boolean) {
        _buscarAmigosState.update { it.copy(isLoadingUser = newValue) }
    }
    fun updateIsSearching(newValue: Boolean) {
        _buscarAmigosState.update { it.copy(isSearching = newValue) }
    }
    fun updateErrorMessage(newValue: String?) {
        _buscarAmigosState.update { it.copy(errorMessage = newValue) }
    }
    fun updateFriendStatus(newValue: String?) {
        _buscarAmigosState.update { it.copy(friendStatus = newValue) }
    }
    fun updateIsSending(newValue: Boolean) {
        _buscarAmigosState.update { it.copy(isSending = newValue) }
    }
    fun updateCurrentUser(newValue: User?) {
        _buscarAmigosState.update { it.copy(currentUser = newValue) }
    }
}