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

@OptIn(ExperimentalCoroutinesApi::class)
class MyRatingsViewModel(private val app: Application) : AndroidViewModel(app) {
    var state: MyRatingsState by mutableStateOf(MyRatingsState())
        private set
    val storage = Storage(app)


    fun loadRatingsOfMe() {
        loadRatings(ofMe = true) {
            state = state.copy(ratingsOfMe = it)
        }
    }

    fun loadRatingsByMe() {
        loadRatings(ofMe = false) {
            state = state.copy(ratingsByMe = it)
        }
    }

    private fun loadRatings(ofMe: Boolean, onLoaded: (List<MyRatingsState.Rating>)->Unit) {
        viewModelScope.launch {
            val runId = storage.getRunId()
            val query = if (ofMe) {
                Database.observeRatingsTo(runId, Auth.getUserId())
            } else {
                Database.observeRatingsFrom(runId, Auth.getUserId())
            }
            query.flatMapLatest { ratings ->
                if (ratings.isEmpty()) {
                    return@flatMapLatest flowOf(listOf())
                }
                combine(ratings.map { rating ->
                    val userId = if (ofMe) rating.from else rating.to
                    Database.observeUser(runId, userId).map { user ->
                        MyRatingsState.Rating(user, rating.stars, rating.createdAt)
                    }
                }) {
                    it.reversed().toList()
                }
            }.collect {
                onLoaded(it)
            }
        }
    }
}