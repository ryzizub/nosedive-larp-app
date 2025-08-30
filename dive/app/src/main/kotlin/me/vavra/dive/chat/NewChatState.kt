package me.vavra.dive.chat

import me.vavra.dive.bottom.User

data class NewChatState(
    val users: List<User> = listOf(),
    val isLoading: Boolean = true
)