package me.vavra.dive.rate

import me.vavra.dive.bottom.User

data class RateState(
    val currentUser: User? = null,
    val ratedUser: User? = null,
    val stars: Int = 0,
    val progress: Progress = Progress.INITIAL
) {
    enum class Progress {
        INITIAL,
        SENDING,
        SUCCESS,
        FAIL
    }
}
