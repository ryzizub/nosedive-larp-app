package me.vavra.dive.rate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.vavra.dive.feed.sampleUsers

class RateViewModel: ViewModel() {
    var state by mutableStateOf(RateState(sampleUsers[0], sampleUsers[1]))
        private set

    fun sendRating() {
        state = state.copy(progress = RateState.Progress.SENDING)
        viewModelScope.launch {
            delay(2000)
            if (Math.random() > 0.5) {
                state = state.copy(progress = RateState.Progress.SUCCESS)
            } else {
                state = state.copy(progress = RateState.Progress.FAIL)
            }
        }
    }

    fun changeStars(stars: Int) {
        state = state.copy(stars = stars)
    }
}