package me.vavra.dive.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Storage

class ConversationViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: ConversationState by mutableStateOf(ConversationState())
        private set
    val storage = Storage(app)

    fun load(conversationId: String) {
        viewModelScope.launch {
            val runId = storage.getRunId()
            conversationId.observeConversation(runId, storage).collect { conversation ->
                state = state.copy(conversation = conversation, isLoading = false)
            }
        }
    }
}