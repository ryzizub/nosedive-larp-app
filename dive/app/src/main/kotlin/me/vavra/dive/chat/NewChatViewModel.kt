package me.vavra.dive.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage
import me.vavra.dive.common.flatMapItems

class NewChatViewModel(private val app: Application) : AndroidViewModel(app) {

    var state by mutableStateOf(NewChatState())
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            val userId = Auth.getUserId()
            Database.observeUsers(storage.getRunId()).collect { users ->
                state = state.copy(
                    users = users.sortedBy { it.name }
                        .filter { it.id != userId },
                    isLoading = false
                )
            }
        }
    }

    fun selectUser(userId: String, onOpenConversation: (String) -> Unit) {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            val runId = storage.getRunId()
            val conversationId =
                Database.observeUserConversations(runId).flatMapItems { conversationId ->
                    Database.observeConversationUsers(runId, conversationId)
                        .map { users -> if (users.contains(userId)) conversationId else null }
                }.first().firstOrNull { it != null }
            if (conversationId == null) {
                val conversationId = Database.addConversation(runId, userId)
                onOpenConversation(conversationId)
            } else {
                onOpenConversation(conversationId)
            }
        }
    }
}