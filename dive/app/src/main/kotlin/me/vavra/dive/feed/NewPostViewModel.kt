package me.vavra.dive.feed

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewPostViewModel: ViewModel() {
    var state by mutableStateOf(NewPostState())
        private set

    fun sendPost() {
        state = state.copy(progress = NewPostState.Progress.SENDING)
        viewModelScope.launch {
            delay(2000)
            if (Math.random() > 0.5) {
                state = state.copy(progress = NewPostState.Progress.SUCCESS)
            } else {
                state = state.copy(progress = NewPostState.Progress.FAIL)
            }
        }
    }

    fun changeText(text: String) {
        state = state.copy(text = text)
    }

    fun changeImageUri(uri: Uri) {
        state = state.copy(imageUri = uri)
    }
}