package me.vavra.dive.chat

import android.Manifest
import android.app.Application
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.vavra.dive.R
import me.vavra.dive.common.Database
import me.vavra.dive.common.MessagingService
import me.vavra.dive.common.Storage

class ReplyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence("replyText")
        val conversationId = intent.getStringExtra("conversationId")
        if (reply != null && conversationId != null) {
            GlobalScope.launch {
                val runId = Storage(context.applicationContext as Application).getRunId()
                Database.addChatMessage(runId, conversationId, reply.toString(), null)
                val repliedNotification = Notification.Builder(context, "chat")
                    .setSmallIcon(R.drawable.ic_notification_chat)
                    .setContentTitle("Odesláno:")
                    .setContentText(reply)
                    .setContentIntent(MessagingService.getConversationPendingIntent(context, conversationId))
                    .setAutoCancel(true)
                    .build()
                with(NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notify(conversationId.hashCode(), repliedNotification)
                    }
                }
            }
        }
    }
}
