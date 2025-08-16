package me.vavra.dive.nearby

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

class NearbyViewModel(private val app: Application): AndroidViewModel(app) {
    var state by mutableStateOf(NearbyState())
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            val userId = Auth.getUserId()
            Database.observeNearbyUsers(storage.getRunId()).collect { users ->
                state = state.copy(
                    nearbyUsers = users.sortedByDescending { it.totalRating }
                        .filter { it.isNearby && it.id != userId }
                )
            }
        }
    }
}