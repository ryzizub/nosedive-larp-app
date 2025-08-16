package me.vavra.dive.nearby

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Auth

class NearbyViewModel() : ViewModel() {
    var state by mutableStateOf(NearbyState())
        private set

    init {
        viewModelScope.launch {
            val userId = Auth.getUserId()
            Database.observeNearbyUsers().collect { users ->
                state = state.copy(
                    nearbyUsers = users.sortedByDescending { it.totalRating }
                        .filter { it.isVisible && it.id != userId }
                )
            }
        }
    }
}