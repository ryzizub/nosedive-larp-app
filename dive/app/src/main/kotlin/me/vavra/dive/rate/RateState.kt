package me.vavra.dive.rate

import me.vavra.dive.User

data class RateState(
    val currentUser: User,
    val ratedUser: User,
    val stars: Int = 0,
    val sent: Boolean = false,
    val success: Boolean = false,
    val fail: Boolean = false
)
