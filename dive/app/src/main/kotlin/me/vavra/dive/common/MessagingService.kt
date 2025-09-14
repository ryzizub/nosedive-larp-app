package me.vavra.dive.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action.SEMANTIC_ACTION_REPLY
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import coil.Coil
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.vavra.dive.MainActivity
import me.vavra.dive.R
import me.vavra.dive.chat.ReplyReceiver
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


class MessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        for (stars in 1..5) {
            createChannel(stars.toString(), "⭐".repeat(stars), "star$stars")
        }
        createChannel("chat", "Zprávy v chatu", "message")
        createChannel("comments", "Komentáře", "comment")
        createChannel("news", "Důležité zprávy", "news")
    }

    private fun createChannel(id: String, name: String, sound: String) {
        val channel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val sound = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/$sound".toUri()
        channel.setSound(sound, audioAttributes)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (Auth.isSignedIn()) {
            val storage = Storage(application)
            GlobalScope.launch {
                Database.updateNotificationsToken(storage.getRunId(), token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("xxx", message.data.toString())
        if (message.data.contains("fromNameGenitiv")) {
            showRatingNotification(message.data)
        } else if (message.data.contains("conversationId")) {
            showChatNotification(message.data)
        } else if (message.data.contains("postText")) {
            showImportantPostNotification(message.data)
        } else if (message.data.contains("postId")) {
            showCommentNotification(message.data)
        }
    }

    private fun showRatingNotification(data: Map<String, String>) {
        val nameGenitiv = data["fromNameGenitiv"]
        val stars = data["stars"]?.toInt() ?: 0
        val starsEmoji = "⭐".repeat(stars)
        val builder = NotificationCompat.Builder(this, stars.toString())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hodnocení od $nameGenitiv")
            .setContentText(starsEmoji)
            .setContentIntent(getRatingsPendingIntent(this))
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }


    private fun showChatNotification(data: Map<String, String>) {
        val authorName = data["authorName"]
        val authorPictureUrl = data["authorPictureUrl"]
        val attachmentUrl = if (data["attachmentUrl"] == "undefined") null else data["attachmentUrl"]
        val messageText = if (attachmentUrl == null) data["messageText"] else data["messageText"] + " (obsahuje přílohu)"
        val conversationId = checkNotNull(data["conversationId"])
        val numericConversationId = conversationId.hashCode()
        // reply
        val remoteInput: RemoteInput = RemoteInput.Builder("replyText")
            .setLabel("Odpovědět")
            .build()
        val replyIntent = Intent(this, ReplyReceiver::class.java).apply {
            putExtra("conversationId", conversationId)
        }
        replyIntent.action = "vavra.me.dive.ACTION_REPLY"
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            numericConversationId,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_reply, "Odpovědět", replyPendingIntent)
            .setSemanticAction(SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput)
            .build()
        GlobalScope.launch {
            val person = Person.Builder().setName(authorName).setIcon(getPersonIconCompat(authorPictureUrl)).build()
            val message = NotificationCompat.MessagingStyle.Message(messageText, System.currentTimeMillis(), person)
            val notification = NotificationCompat.Builder(this@MessagingService, "chat")
                .setSmallIcon(R.drawable.ic_notification_chat)
                .setStyle(
                    NotificationCompat.MessagingStyle(person).addMessage(message)
                ).setContentIntent(getConversationPendingIntent(this@MessagingService, conversationId))
                .addAction(replyAction)
                .setAutoCancel(true)
                .setGroup("chat")
                .build()
            with(NotificationManagerCompat.from(this@MessagingService)) {
                if (ActivityCompat.checkSelfPermission(
                        this@MessagingService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(numericConversationId, notification)
                }
            }
        }
    }

    private fun showCommentNotification(data: Map<String, String>) {
        val authorName = data["authorName"]
        val authorPictureUrl = data["authorPictureUrl"]
        val attachmentUrl = if (data["attachmentUrl"] == "undefined") null else data["attachmentUrl"]
        val messageText = if (attachmentUrl == null) data["messageText"] else data["messageText"] + " (obsahuje přílohu)"
        val postId = checkNotNull(data["postId"])
        val numericPostId = postId.hashCode()
        GlobalScope.launch {
            val person = Person.Builder().setName(authorName).setIcon(getPersonIconCompat(authorPictureUrl)).build()
            val message = NotificationCompat.MessagingStyle.Message(messageText, System.currentTimeMillis(), person)
            val notification = NotificationCompat.Builder(this@MessagingService, "comments")
                .setSmallIcon(R.drawable.ic_notification_feed)
                .setStyle(
                    NotificationCompat.MessagingStyle(person).addMessage(message)
                ).setContentIntent(getCommentsPendingIntent(this@MessagingService, postId))
                .setAutoCancel(true)
                .setGroup("comments")
                .build()
            with(NotificationManagerCompat.from(this@MessagingService)) {
                if (ActivityCompat.checkSelfPermission(
                        this@MessagingService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(numericPostId, notification)
                }
            }
        }
    }


    private fun showImportantPostNotification(data: Map<String, String>) {
        val authorName = data["authorName"]
        val authorPictureUrl = data["authorPictureUrl"]
        val postText = data["postText"]
        GlobalScope.launch {
            val notification = NotificationCompat.Builder(this@MessagingService, "news")
                .setSmallIcon(R.drawable.ic_notification_feed)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(postText)
                )
                .setContentIntent(getFeedPendingIntent(this@MessagingService))
                .setContentTitle(authorName)
                .setContentText(postText)
                .setLargeIcon(getPersonIcon(authorPictureUrl))
                .setAutoCancel(true)
                .setGroup("news")
                .build()
            with(NotificationManagerCompat.from(this@MessagingService)) {
                if (ActivityCompat.checkSelfPermission(
                        this@MessagingService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(Random.nextInt(), notification)
                }
            }
        }
    }

    private suspend fun getPersonIconCompat(
        imageUrl: String?
    ): IconCompat? = suspendCoroutine { continuation ->
        if (imageUrl == null) {
            continuation.resume(null)
        } else {
            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .target { drawable ->
                    continuation.resume(
                        IconCompat.createWithBitmap((drawable as BitmapDrawable).bitmap)
                    )
                }
                .listener(object : ImageRequest.Listener {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        continuation.resume(null)
                    }
                })
                .build()
            Coil.imageLoader(this).enqueue(request)
        }
    }

    private suspend fun getPersonIcon(
        imageUrl: String?
    ): Icon? = suspendCoroutine { continuation ->
        if (imageUrl == null) {
            continuation.resume(null)
        } else {
            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .target { drawable ->
                    continuation.resume(
                        Icon.createWithBitmap((drawable as BitmapDrawable).bitmap)
                    )
                }
                .listener(object : ImageRequest.Listener {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        continuation.resume(null)
                    }
                })
                .build()
            Coil.imageLoader(this).enqueue(request)
        }
    }

    companion object {
        fun getConversationPendingIntent(context: Context, conversationId: String): PendingIntent {
            return getNotificationIntent(context, "conversation/$conversationId")
        }

        fun getCommentsPendingIntent(context: Context, postId: String): PendingIntent {
            return getNotificationIntent(context, "comments/$postId")
        }

        fun getFeedPendingIntent(context: Context): PendingIntent {
            return getNotificationIntent(context, "feed")
        }

        fun getRatingsPendingIntent(context: Context): PendingIntent {
            return getNotificationIntent(context, "ratings")
        }

        private fun getNotificationIntent(context: Context, data: String): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                setData("dive://$data".toUri())
            }
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
    }
}