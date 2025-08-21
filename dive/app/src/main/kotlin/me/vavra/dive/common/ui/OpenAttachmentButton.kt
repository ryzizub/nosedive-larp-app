package me.vavra.dive.common.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun OpenAttachmentButton(url: String?) {
    if (url != null) {
        Spacer(modifier = Modifier.height(8.dp))
        val activity = LocalActivity.current
        FilledTonalButton(onClick = {
            val webpage: Uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            try {
                activity?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Tuto přílohu nelze otevřít", Toast.LENGTH_LONG).show()
            }
        }) { Text("Otevřít přílohu") }
    }
}