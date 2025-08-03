@file:OptIn(ExperimentalCoroutinesApi::class)

package me.vavra.dive

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth

class MainViewModel: ViewModel() {

    var state: MainState by mutableStateOf(MainState.Loading)
        private set

    init {
        viewModelScope.launch {
            Auth.observeUserId().flatMapLatest { userId ->
                if (userId == null) {
                    flowOf( MainState.LoggedOut)
                } else {
                    Database.observeUser(userId).map { MainState.LoggedIn(it.shortenName()) }
                }
            }.collect {
                state = it
            }
        }
    }

    private fun User.shortenName(): User {
        return this.copy(name = this.name.split(" ")[0])
    }

    fun login(password: String) {
        state = MainState.Loading
        viewModelScope.launch {
            val success = Auth.login(password)
            if (!success) {
                state = MainState.LoggedOut
            }
        }
    }

    fun logOut() {
        Auth.logout()
    }
}