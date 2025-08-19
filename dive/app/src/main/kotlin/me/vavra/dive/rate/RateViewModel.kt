package me.vavra.dive.rate

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

class RateViewModel(private val app: Application): AndroidViewModel(app) {
    var state: RateState by mutableStateOf(RateState())
        private set
    val storage = Storage(app)

    fun load(userId: String) {
        viewModelScope.launch {
            val runId = storage.getRunId()
            combine(Database.observeUser(runId, Auth.getUserId()), Database.observeUser(runId, userId)) {
                currentUser, ratedUser ->
                Pair(currentUser, ratedUser)
            }.collect { (currentUser, ratedUser) ->
                state = state.copy(currentUser = currentUser, ratedUser = ratedUser)
            }
        }
    }

    fun sendRating() {
        state = state.copy(progress = RateState.Progress.SENDING)
        if (Database.isOnline(app)) {
            viewModelScope.launch {
                val runId = storage.getRunId()
                Database.addRating(runId, checkNotNull(state.currentUser).id, checkNotNull(state.ratedUser).id, state.stars)
                state = state.copy(progress = RateState.Progress.SUCCESS)
            }
        } else {
            state = state.copy(progress = RateState.Progress.FAIL)
        }
    }

    fun changeStars(stars: Int) {
        state = state.copy(stars = stars)
    }
}