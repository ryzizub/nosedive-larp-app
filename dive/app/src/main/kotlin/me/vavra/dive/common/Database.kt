package me.vavra.dive.common

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import me.vavra.dive.Run
import me.vavra.dive.User
import java.math.RoundingMode
import java.text.DecimalFormat

object Database {
    init {
        Firebase.database.setPersistenceEnabled(true)
    }

    private val reference = Firebase.database.reference
    private val ratingFormat =
        DecimalFormat("0.000").apply { this.roundingMode = RoundingMode.HALF_UP }

    fun observeUsers(runId: String): Flow<List<User>> {
        val query = reference.child("users/$runId")
        return query.snapshots.map { list ->
            list.children.map { snapshot ->
                snapshot.toUser()
            }
        }
    }

    fun observeUser(runId: String, userId: String): Flow<User> {
        return reference.child("users/$runId").child(userId).snapshots.map {
            it.toUser()
        }
    }

    fun observeRuns(): Flow<List<Run>> {
        return reference.child("runs").snapshots.map { list ->
            list.children.map { snapshot ->
                Run(checkNotNull(snapshot.key), checkNotNull(snapshot.child("name").getValue<String>()))
            }
        }
    }

    fun observeRatingsFrom(runId: String, userId: String): Flow<List<Rating>> {
        return loadRatings(runId, userId, "from")
    }

    fun observeRatingsTo(runId: String, userId: String): Flow<List<Rating>> {
        return loadRatings(runId, userId, "to")
    }

    fun observePosts(runId: String): Flow<List<Post>> {
        return reference.child("posts/$runId").snapshots.map {
            it.children.mapNotNull { snap ->
                snap.getValue<Post>()?.copy(id = checkNotNull(snap.key))
            }
        }
    }

    suspend fun addRating(
        runId: String,
        from: String,
        to: String,
        stars: Int
    ) {
        reference.child("ratings/$runId").push().updateChildren(
            hashMapOf(
                "from" to from,
                "to" to to,
                "stars" to stars,
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    suspend fun addPost(
        runId: String,
        text: String,
        pictureUrl: String,
    ) {
        reference.child("posts/$runId").push().updateChildren(
            hashMapOf(
                "author" to Auth.getUserId(),
                "text" to text,
                "pictureUrl" to pictureUrl,
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    fun updateNotificationsToken(runId: String, token: String) {
        val uid = Firebase.auth.uid
        if (uid != null) {
            Log.d("FCM token", token)
            reference.child("userSecrets/$runId").child(uid).updateChildren(
                hashMapOf(
                    "notificationsToken" to token
                ) as Map<String, Any>
            )
        }
    }

    fun isOnline(app: Application): Boolean {
        val connectivityManager = app.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
    }

    private fun loadRatings(runId: String, userId: String, orderByChild: String): Flow<List<Rating>> {
        return reference.child("ratings/$runId").orderByChild(orderByChild).equalTo(userId).snapshots.map {
            it.children.mapNotNull { snap ->
                snap.getValue<Rating>()
            }
        }
    }

    private fun DataSnapshot.toUser(): User {
        val totalRating = child("totalRating").getValue<Double>() ?: 0.0
        return User(
            id = checkNotNull(key),
            name = checkNotNull(child("name").getValue<String>()),
            nameVokativ = child("nameVokativ").getValue<String>() ?: "",
            nameAkuzativ = child("nameAkuzativ").getValue<String>() ?: "",
            nameGenitiv = child("nameGenitiv").getValue<String>() ?: "",
            profilePictureUrl = checkNotNull(
                child("profilePictureUrl").getValue<String>()
            ),
            totalRating = totalRating,
            mainRating = totalRating.formatToOnceDecimal(),
            detailedRating = totalRating.extractThirdAndFourthDecimal(),
            isNearby = checkNotNull(child("isNearby").getValue<Boolean>())
        )
    }

    private fun Double.formatToOnceDecimal(): String {
        val formatted = ratingFormat.format(this)
        return formatted.substring(0, 3)
    }

    private fun Double.extractThirdAndFourthDecimal(): String {
        val formatted = ratingFormat.format(this)
        return formatted.substring(3, 5)
    }

    data class Rating(
        val from: String = "",
        val to: String = "",
        val stars: Int = 0,
        val createdAt: Long = 0
    )

    data class Post(
        val id: String = "",
        val text: String = "",
        val author: String = "",
        val pictureUrl: String = "",
        val createdAt: Long = 0
    )
}