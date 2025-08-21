package me.vavra.dive.feed

data class CommentsState(
    val post: FeedState.Post? = null,
    val sendingComment: Boolean = false
)
