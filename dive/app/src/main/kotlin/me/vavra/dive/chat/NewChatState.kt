package me.vavra.dive.chat

import me.vavra.dive.User

data class NewChatState(
    val users: List<User> = listOf()
)