package me.vavra.dive.bottom

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

class UserViewModel(private val app: Application) : AndroidViewModel(app) {

    var state by mutableStateOf(BottomNavigationState())
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observeUser(runId, Auth.getUserId()).collect { user ->
                state = this@UserViewModel.state.copy(user = user.shortenName(), isLoading = false)
            }
        }
    }

    fun logOut() {
        Auth.logout()
    }

    private fun User.shortenName(): User {
        return this.copy(name = this.name.split(" ")[0])
    }
}