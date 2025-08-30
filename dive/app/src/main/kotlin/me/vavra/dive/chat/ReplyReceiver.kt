package me.vavra.dive.chat

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.vavra.dive.common.Database
import me.vavra.dive.common.Storage

class ReplyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence("replyText")
        val conversationId = intent.getStringExtra("conversationId")
        if (reply != null && conversationId != null) {
            GlobalScope.launch {
                val runId = Storage(context.applicationContext as Application).getRunId()
                Database.addChatMessage(runId, conversationId, reply.toString(), null)
            }
        }
    }
}
