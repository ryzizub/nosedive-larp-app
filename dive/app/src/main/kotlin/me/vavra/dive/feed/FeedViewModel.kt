package me.vavra.dive.feed

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Database.toMessages
import me.vavra.dive.common.Storage

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(private val app: Application) : AndroidViewModel(app) {

    var state: FeedState by mutableStateOf(FeedState())
        private set
    private val storage = Storage(app)
    private var rateJobs: MutableMap<String, Job> = mutableMapOf()

    init {
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observePosts(runId).flatMapLatest { posts ->
                if (posts.isEmpty()) {
                    return@flatMapLatest flowOf(listOf())
                }
                combine(posts.map { post ->
                    post.toFeedPost(runId).map { it.copy(comments = it.comments.reversed()) }
                }) {
                    it.reversed().toList()
                }
            }.collect {
                state = state.copy(posts = it, isLoading = false)
            }
        }
    }

    fun rate(postId: String, stars: Int) {
        state = state.copy(posts = state.posts.map { if (it.id == postId) it.copy(stars = stars) else it })
        rateJobs[postId]?.cancel()
        rateJobs[postId] = viewModelScope.launch {
            delay(2000)
            val runId = storage.getRunId()
            Database.addPostRating(runId, postId, stars)
        }
    }
}

fun Database.Post.toFeedPost(runId: String): Flow<FeedState.Post> {
    return combine(
        Database.observeUser(runId, author),
        Database.observePostRating(runId, id),
        Database.observePostComments(runId, id).toMessages(runId)
    ) { user, stars, comments ->
        val rated = stars != null
        FeedState.Post(id, user, pictureUrl, text, stars ?: 0, rated, comments)
    }
}