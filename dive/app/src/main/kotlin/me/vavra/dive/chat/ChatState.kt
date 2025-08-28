package me.vavra.dive.chat

import me.vavra.dive.User
import me.vavra.dive.common.Database

data class ChatState(
    val conversations: List<Conversation> = listOf(),
    val isLoading: Boolean = true
) {
    data class Conversation(
        val id: String,
        val partner: User,
        val messages: List<Database.Message>,
        val unread: Boolean
    )
}
