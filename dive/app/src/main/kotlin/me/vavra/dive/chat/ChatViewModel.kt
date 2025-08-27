package me.vavra.dive.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Database.toMessages
import me.vavra.dive.common.Storage
import me.vavra.dive.common.flatMapItems

class ChatViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: ChatState by mutableStateOf(ChatState())
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observeUserConversations(runId)
                .flatMapItems { it.observeConversation(runId, storage) }
                .collect {
                    state = state.copy(conversations = it, isLoading = false)
                }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun String.observeConversation(runId: String, storage: Storage): Flow<ChatState.Conversation> {
    return combine(
        Database.observeConversationUsers(runId, this),
        Database.observeConversationMessages(runId, this).toMessages(runId),
        storage.observeConversationReadCount(runId, this)
    ) { users, messages, readCount ->
        Triple(users, messages, readCount)
    }.flatMapLatest { (users, messages, readCount) ->
        val partnerId = users.first { it != Auth.getUserId() }
        Database.observeUser(runId, partnerId).map { partner ->
            ChatState.Conversation(partner, messages, readCount != messages.size)
        }
    }
}