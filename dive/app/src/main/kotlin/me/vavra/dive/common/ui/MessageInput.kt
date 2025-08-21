package me.vavra.dive.common.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun MessageInput(
    hint: String,
    sending: Boolean,
    onSend: (String, Uri?) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .windowInsetsPadding(NavigationBarDefaults.windowInsets),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text(hint) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend(message, selectedUri)
                    message = ""
                    selectedUri = null
                    keyboardController?.hide()
                }
            ),
            enabled = !sending
        )
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(onClick = {
            if (selectedUri == null) {
                filePickerLauncher.launch("*/*")
            } else {
                selectedUri = null
            }
        }) {
            if (sending) {
                InlinedLoadingIndicator()
            } else {
                if (selectedUri == null) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach any file"
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear selected file"
                    )
                }
            }
        }
    }
}
