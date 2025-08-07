package me.vavra.dive.feed

import android.net.Uri

data class NewPostState(
    val text: String = "",
    val imageUri: Uri? = null,
    val progress: Progress = Progress.INITIAL
) {
    enum class Progress {
        INITIAL,
        SENDING,
        SUCCESS,
        FAIL
    }
}
