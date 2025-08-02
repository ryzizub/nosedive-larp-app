package me.vavra.dive.common

import android.util.JsonReader
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import me.vavra.dive.Database
import java.net.HttpURLConnection
import java.net.URL

object Auth {

    fun observeUserId(): Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.uid)
        }
        Firebase.auth.addAuthStateListener(authStateListener)
        awaitClose {
            Firebase.auth.removeAuthStateListener(authStateListener)
        }
    }

    fun getUserId(): String {
        return Firebase.auth.uid ?: throw IllegalStateException("User not logged in")
    }

    suspend fun login(password: String): Boolean {
        withContext(Dispatchers.IO) {
            try {
                val url =
                    URL("https://europe-west1-nosedive-larp.cloudfunctions.net/login?password=$password")
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == 200) {
                    val reader = JsonReader(connection.inputStream.reader())
                    reader.beginObject()
                    reader.nextName()
                    val token = reader.nextString()
                    reader.nextName()
                    val invalidPassword = reader.nextBoolean()
                    reader.endObject()
                    reader.close()
                    if (!invalidPassword) {
                        Firebase.auth.signInWithCustomToken(token).await()
                        updateNotificationsToken()
                        return@withContext true
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return false
    }

    private suspend fun updateNotificationsToken() {
        val token = FirebaseMessaging.getInstance().token.await()
        Database.updateNotificationsToken(token)
    }

    fun logout() {
        Firebase.auth.signOut()
    }
}