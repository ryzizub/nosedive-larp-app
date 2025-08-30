package me.vavra.dive.common

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object Files {
    suspend fun upload(uri: Uri, folder: String, runId: String): String {
        val storage = FirebaseStorage.getInstance()
        val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment ?: "file"}"
        val fileRef = storage.reference.child("$folder/$runId/$fileName")
        fileRef.putFile(uri).await()
        val downloadUrl = fileRef.downloadUrl.await()
        return downloadUrl.toString()
    }

    fun getOpenAttachmentIntent(url: String): Intent {
        val uri: Uri = url.toUri()
        return Intent(Intent.ACTION_VIEW, uri)
    }
}
