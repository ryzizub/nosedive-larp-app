package me.vavra.dive.ratings

import me.vavra.dive.User

data class MyRatingsState(
    val ratingsOfMe: List<Rating> = listOf(),
    val ratingsByMe: List<Rating> = listOf(),
    val isLoading: Boolean = true
) {
    data class Rating(
        val user: User, // The other user involved in the rating
        val stars: Int,
        val timestamp: Long,
    )
}
