package me.vavra.dive

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth

class MainViewModel: ViewModel() {

    var state by mutableStateOf(MainState.LOADING)
        private set

    init {
        viewModelScope.launch {
            Auth.observeUserId().collect {
                state = if (it == null) {
                    MainState.LOGGED_OUT
                } else {
                    MainState.LOGGED_IN
                }
            }
        }
    }

    fun login(password: String) {
        state = MainState.LOADING
        viewModelScope.launch {
            val success = Auth.login(password)
            if (!success) {
                state = MainState.LOGGED_OUT
            }
        }
    }
}