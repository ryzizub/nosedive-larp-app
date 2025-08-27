package me.vavra.dive

import kotlinx.serialization.Serializable

sealed class NavDestination {
    @Serializable
    object Nearby : NavDestination()
    @Serializable
    object Feed : NavDestination()
    @Serializable
    object Chat : NavDestination()
    @Serializable
    data class Conversation(val conversationId: String) : NavDestination()
    @Serializable
    data class Comments(val postId: String) : NavDestination()
    @Serializable
    object MyRatings : NavDestination()
    @Serializable
    data class Rate(val userId: String) : NavDestination()
    @Serializable
    object NewPost: NavDestination()
    @Serializable
    object NewChat: NavDestination()
}