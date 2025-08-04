package me.vavra.dive.ratings

import me.vavra.dive.User
import me.vavra.dive.feed.sampleUsers

data class MyRatingsState(
    val ratingsOfMe: List<Rating> = listOf(
        Rating("1", sampleUsers[1], 5, System.currentTimeMillis() - 100000000),
        Rating("2", sampleUsers[2], 3, System.currentTimeMillis() - 200000000)
    ), val ratingsByMe: List<Rating> = listOf(
        Rating("3", sampleUsers[1], 4, System.currentTimeMillis() - 300000000),
        Rating("4", sampleUsers[1], 5, System.currentTimeMillis() - 400000000)
    )
) {
    data class Rating(
        val id: String,
        val user: User, // The other user involved in the rating
        val stars: Int,
        val timestamp: Long,
    )
}
