package me.vavra.dive.chat

data class ConversationState(
    val conversation: ChatState.Conversation? = null,
    val isLoading: Boolean = true,
    val sendingMessage: Boolean = false
)
