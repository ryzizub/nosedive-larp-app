package me.vavra.dive.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
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
            val channel = NotificationChannel(
                stars.toString(),
                "Hodnocení " + "⭐".repeat(stars),
                NotificationManager.IMPORTANCE_HIGH
            )
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val sound = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/star$stars".toUri()
            channel.setSound(sound, audioAttributes)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val channel = NotificationChannel(
            "chat",
            "Zprávy v chatu",
            NotificationManager.IMPORTANCE_HIGH
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val sound = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/raw/message.mp3".toUri()
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
        Log.d("Dive", message.data.toString())
        if (message.data.contains("fromNameGenitiv")) {
            showRatingNotification(message.data)
        } else if (message.data.contains("conversationId")) {
            showChatNotification(message.data)
        }

    }

    private fun showRatingNotification(data: Map<String, String>) {
        val nameGenitiv = data["fromNameGenitiv"]
        val stars = data["stars"]?.toInt() ?: 0
        val starsEmoji = "⭐".repeat(stars)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, stars.toString())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hodnocení od $nameGenitiv")
            .setContentText(starsEmoji)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val notificationId = Random.Default.nextInt()
            notify(notificationId, builder.build())
        }
    }


    private fun showChatNotification(data: Map<String, String>) {
        val authorName = data["authorName"]
        val authorPictureUrl = data["authorPictureUrl"]
        val messageText = data["messageText"]
        val attachmentUrl = data["attachmentUrl"]
        val conversationId = data["conversationId"]
        val numericConversationId = conversationId.hashCode()
        // content
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("conversationId", conversationId)
        }
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
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
        // open attachment
        val openAttachmentAction = if (attachmentUrl != null) {
            val openAttachmentIntent = Files.getOpenAttachmentIntent(attachmentUrl)
            val openAttachmentPendingIntent: PendingIntent =
                PendingIntent.getActivity(this, 0, openAttachmentIntent, PendingIntent.FLAG_MUTABLE)
            NotificationCompat.Action.Builder(R.drawable.ic_open_attachment, "Otevřít přílohu", openAttachmentPendingIntent)
                .addRemoteInput(remoteInput)
                .build()
        } else {
            null
        }
        GlobalScope.launch {
            val person = Person.Builder().setName(authorName).setIcon(getPersonIcon(authorPictureUrl)).build()
            val message = NotificationCompat.MessagingStyle.Message(messageText, System.currentTimeMillis(), person)
            val notification = NotificationCompat.Builder(this@MessagingService, "chat")
                .setSmallIcon(R.drawable.ic_notification_chat)
                .setStyle(
                    NotificationCompat.MessagingStyle(person).addMessage(message)
                ).setContentIntent(contentPendingIntent)
                .addAction(replyAction)
                .apply {
                    if (openAttachmentAction != null) {
                        addAction(openAttachmentAction)
                    }
                }.setAutoCancel(true)
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

    private suspend fun getPersonIcon(
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
}