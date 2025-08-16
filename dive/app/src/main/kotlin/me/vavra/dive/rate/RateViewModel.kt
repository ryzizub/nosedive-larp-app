package me.vavra.dive.rate

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage
import me.vavra.dive.feed.sampleUsers

class RateViewModel(private val app: Application): AndroidViewModel(app) {
    var state by mutableStateOf(RateState(sampleUsers[0], sampleUsers[1]))
        private set
    val storage = Storage(app)

    fun sendRating() {
        state = state.copy(progress = RateState.Progress.SENDING)
        if (Database.isOnline(app)) {
            viewModelScope.launch {
                val runId = storage.getRunId()
                Database.addRating(runId, state.currentUser.id, state.ratedUser.id, state.stars)
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