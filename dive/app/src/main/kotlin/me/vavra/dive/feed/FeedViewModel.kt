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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
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
                    combine(
                        Database.observeUser(runId, post.author),
                        Database.observePostRating(runId, post.id)
                    ) { user, stars ->
                        val rated = stars != null
                        FeedState.Post(post.id, user, post.pictureUrl, post.text, stars ?: 0, rated, listOf())
                    }
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