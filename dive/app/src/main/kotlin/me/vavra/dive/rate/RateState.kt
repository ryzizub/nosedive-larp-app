package me.vavra.dive.rate

import me.vavra.dive.User

data class RateState(
    val currentUser: User,
    val ratedUser: User,
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
