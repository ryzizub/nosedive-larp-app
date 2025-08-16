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

    fun isSignedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }

    suspend fun login(runId: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val url =
                    URL("https://europe-west1-nosedive-larp.cloudfunctions.net/login?password=$password&run=$runId")
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
                        updateNotificationsToken(runId)
                        true
                    } else {
                         false
                    }
                } else {
                    false
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun updateNotificationsToken(runId: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        Database.updateNotificationsToken(runId, token)
    }

    fun logout() {
        Firebase.auth.signOut()
    }
}