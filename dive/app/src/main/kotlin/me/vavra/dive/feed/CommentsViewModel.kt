package me.vavra.dive.feed

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Files
import me.vavra.dive.common.Storage

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: CommentsState by mutableStateOf(CommentsState())
        private set
    val storage = Storage(app)
    private lateinit var postId: String

    fun load(postId: String) {
        this.postId = postId
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observePost(runId, postId).flatMapLatest { post -> post.toFeedPost(runId) }.collect {
                state = state.copy(post = it)
            }
        }
    }

    fun addComment(text: String, attachmentUri: Uri?) {
        state = state.copy(sendingComment = true)
        viewModelScope.launch {
            val runId = storage.getRunId()
            val attachmentUrl = if (attachmentUri != null) {
                Files.upload(attachmentUri, "comment_attachments", runId)
            } else {
                null
            }
            Database.addPostComment(runId, postId, text, attachmentUrl)
            state = state.copy(sendingComment = false)
        }
    }
}