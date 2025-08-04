package me.vavra.dive.nearby

import me.vavra.dive.User

data class NearbyState(
    val nearbyUsers: List<User> = listOf()
)