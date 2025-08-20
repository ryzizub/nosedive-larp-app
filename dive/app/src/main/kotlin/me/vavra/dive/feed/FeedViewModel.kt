package me.vavra.dive.feed

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: FeedState by mutableStateOf(FeedState())
        private set
    val storage = Storage(app)

    init {
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observePosts(runId).flatMapLatest { posts ->
                if (posts.isEmpty()) {
                    return@flatMapLatest flowOf(listOf())
                }
                combine(posts.map { post ->
                    Database.observeUser(runId, post.author).map { user ->
                        FeedState.Post(post.id, user, post.pictureUrl, post.text, listOf())
                    }
                }) {
                    it.reversed().toList()
                }
            }.collect {
                state = state.copy(posts = it, isLoading = false)
            }
        }
    }
}