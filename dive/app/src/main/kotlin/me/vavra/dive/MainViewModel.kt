@file:OptIn(ExperimentalCoroutinesApi::class)

package me.vavra.dive

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

class MainViewModel(private val app: Application): AndroidViewModel(app) {

    var state: MainState by mutableStateOf(MainState.Loading)
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            Auth.observeUserId().flatMapLatest { userId ->
                if (userId == null) {
                    Database.observeRuns().map { MainState.LoggedOut(it) }
                } else {
                    val runId = storage.getRunId()
                    Database.observeUser(runId, userId).map { MainState.LoggedIn(it.shortenName(), runId) }
                }
            }.collect {
                state = it
            }
        }
    }

    private fun User.shortenName(): User {
        return this.copy(name = this.name.split(" ")[0])
    }

    fun login(runId: String, password: String) {
        val loggedOutState = state
        state = MainState.Loading
        viewModelScope.launch {
            storage.saveRunId(runId)
            val success = Auth.login(runId, password)
            if (!success) {
                state = loggedOutState
            }
        }
    }

    fun logOut() {
        Auth.logout()
    }
}