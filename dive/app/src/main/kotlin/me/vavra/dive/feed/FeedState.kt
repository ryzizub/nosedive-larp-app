package me.vavra.dive.feed

import me.vavra.dive.bottom.User
import me.vavra.dive.common.Database

data class FeedState(
    val posts: List<Post> = listOf(),
    val isLoading: Boolean = true
) {

    data class Post(
        val id: String,
        val user: User,
        val imageUrl: String?,
        val text: String,
        val stars: Int,
        val rated: Boolean,
        val comments: List<Database.Message>
    )
}
