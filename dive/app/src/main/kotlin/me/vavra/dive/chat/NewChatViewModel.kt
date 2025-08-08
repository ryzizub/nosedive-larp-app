package me.vavra.dive.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.Database
import me.vavra.dive.common.Auth

class NewChatViewModel() : ViewModel() {
    var state by mutableStateOf(NewChatState())
        private set

    init {
        viewModelScope.launch {
            val userId = Auth.getUserId()
            Database.observeNearbyUsers().collect { users ->
                state = state.copy(
                    users = users.sortedBy { it.name }
                        .filter { it.id != userId }
                )
            }
        }
    }
}