package me.vavra.dive.ratings

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
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

class MyRatingsViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: MyRatingsState by mutableStateOf(MyRatingsState())
        private set
    val storage = Storage(app)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadRatingsByMe() {
        viewModelScope.launch {
            val runId = storage.getRunId()
            Database.observeRatingsFrom(runId, Auth.getUserId()).flatMapLatest { ratings ->
                if (ratings.isEmpty()) {
                    return@flatMapLatest flowOf(listOf())
                }
                combine(ratings.map { rating ->
                    Database.observeUser(runId, rating.to).map { user ->
                        MyRatingsState.Rating(user, rating.stars, rating.createdAt)
                    }
                }) {
                    it.toList()
                }
            }.collect {
                state = state.copy(ratingsByMe = it)
            }
        }
    }
}