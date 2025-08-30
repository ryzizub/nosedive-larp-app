package me.vavra.dive.chat

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Files
import me.vavra.dive.common.Storage

class ConversationViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: ConversationState by mutableStateOf(ConversationState())
        private set
    val storage = Storage(app)
    private lateinit var conversationId: String

    fun load(conversationId: String) {
        this.conversationId = conversationId
        viewModelScope.launch {
            val runId = storage.getRunId()
            conversationId.observeConversation(runId, storage).collect { conversation ->
                state = state.copy(conversation = conversation.copy(messages = conversation.messages.reversed()), isLoading = false)
                storage.saveConversationReadCount(runId, conversationId, conversation.messages.size)
            }
        }
    }

    fun sendMessage(text: String, attachmentUri: Uri?) {
        state = state.copy(sendingMessage = true)
        viewModelScope.launch {
            val runId = storage.getRunId()
            val attachmentUrl = if (attachmentUri != null) {
                Files.upload(attachmentUri, "chat_attachments", runId)
            } else {
                null
            }
            Database.addChatMessage(runId, conversationId, text, attachmentUrl)
            state = state.copy(sendingMessage = false)
        }
    }
}