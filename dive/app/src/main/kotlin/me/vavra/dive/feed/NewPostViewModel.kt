package me.vavra.dive.feed

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.vavra.dive.R
import me.vavra.dive.common.Audio
import me.vavra.dive.common.Database
import me.vavra.dive.common.Files
import me.vavra.dive.common.Storage

class NewPostViewModel(private val app: Application) : AndroidViewModel(app) {
    var state by mutableStateOf(NewPostState())
        private set
    val storage = Storage(app)
    val audio = Audio(app)

    fun sendPost() {
        state = state.copy(progress = NewPostState.Progress.SENDING)
        if (Database.isOnline(app)) {
            viewModelScope.launch {
                try {
                    val runId = storage.getRunId()
                    val imageUri = state.imageUri
                    val pictureUrl = if (imageUri != null) {
                        Files.upload(imageUri, "feed_pics", runId)
                    } else null
                    Database.addPost(runId, state.text, pictureUrl)
                    audio.play(R.raw.swoosh)
                    state = state.copy(progress = NewPostState.Progress.SUCCESS)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    sendingFailed()
                }
            }
        } else {
            sendingFailed()
        }
    }

    private fun sendingFailed() {
        audio.play(R.raw.error)
        state = state.copy(progress = NewPostState.Progress.FAIL)
    }

    fun changeText(text: String) {
        state = state.copy(text = text)
    }

    fun changeImageUri(uri: Uri) {
        state = state.copy(imageUri = uri)
    }
}