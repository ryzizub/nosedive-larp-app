package me.vavra.dive.common.ui

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.vavra.dive.common.Files

@Composable
fun OpenAttachmentButton(url: String?, tonal: Boolean) {
    if (url != null) {
        Spacer(modifier = Modifier.height(8.dp))
        val activity = LocalActivity.current
        val onClick: () -> Unit = {
            val intent = Files.getOpenAttachmentIntent(url)
            try {
                activity?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Tuto přílohu nelze otevřít", Toast.LENGTH_LONG).show()
            }
        }
        if (tonal) {
            FilledTonalButton(onClick = onClick) { Text("Otevřít přílohu") }
        } else {
            OutlinedButton(onClick = onClick) { Text("Otevřít přílohu") }
        }
    }
}