package me.vavra.dive.nearby

import me.vavra.dive.User

data class NearbyState(
    val nearbyUsers: List<User> = listOf()
)

data class Rating(
    val ofUser: User,
    val stars: Int = 0,
    val sent: Boolean = false,
    val success: Boolean = false,
    val fail: Boolean = false
)