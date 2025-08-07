package me.vavra.dive.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.BottomSheetTopBar
import me.vavra.dive.rate.RateScreenContent
import me.vavra.dive.rate.RateViewModel
import kotlin.math.roundToInt


@Composable
fun NewPostScreen() {
    val viewModel = viewModel<NewPostViewModel>()
    NewPostScreenContent(viewModel.state, onSend = {
        viewModel.sendPost()
    }, onChangeText = {
        viewModel.changeText(it)
    }, onChangeImageUri = {
        viewModel.changeImageUri(it)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreenContent(state: NewPostState, onChangeText: (String) -> Unit = {}, onChangeImageUri: (Uri) -> Unit = {}, onSend: () -> Unit = {  }) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                onChangeImageUri(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            BottomSheetTopBar("Nový příspěvek")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp) // Outer padding for the content
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = !hasSent
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { imageUri = null }, enabled = !hasSent) {
                    Text("Remove Image")
                }
            } else {
                Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !hasSent) {
                    Text("Attach Image")
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push sender to the bottom

            val canSend = (postText.isNotBlank() || imageUri != null) && !hasSent

            ListItem(
                headlineContent = { Text("Send Post", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                supportingContent = { Text("drag up to send", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                leadingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Send") },
                trailingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Send") },
                modifier = Modifier
                    .fillMaxWidth() // Ensure ListItem takes full width for consistent border
                    .offset { IntOffset(0, listItemOffsetY.value.roundToInt()) }
                    .draggable(
                        orientation = Orientation.Vertical,
                        enabled = canSend, // Only draggable if canSend is true
                        state = rememberDraggableState { delta ->
                            if (canSend) { // Check again in case state changed during drag start
                                scope.launch {
                                    val newOffset = (listItemOffsetY.value + delta).coerceAtMost(0f)
                                    listItemOffsetY.snapTo(newOffset)
                                }
                            }
                        },
                        onDragStopped = { velocity ->
                            if (canSend) {
                                scope.launch {
                                    val currentOffset = listItemOffsetY.value
                                    if (currentOffset < sendThresholdPx || velocity < -400f) {
                                        hasSent = true // Mark as sent
                                        listItemOffsetY.animateTo(sentItemRestingOffsetPx, animationSpec = tween(150))
                                        onSendPost(postText, imageUri)
                                        // Item stays at sentItemRestingOffsetPx
                                    } else {
                                        listItemOffsetY.animateTo(0f, animationSpec = tween(300))
                                    }
                                }
                            }
                        }
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (canSend) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(2.dp)
                    ),
                colors = androidx.compose.material3.ListItemDefaults.colors(
                    containerColor = if (canSend) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    headlineColor = if(canSend) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    supportingColor = if(canSend) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    leadingIconColor = if(canSend) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    trailingIconColor = if(canSend) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPostScreenPreview() {
    DiveTheme {
        NewPostScreen(onSendPost = { text, uri ->
            println("Preview: Send Post - Text: '$text', ImageURI: $uri")
        }, onNavigateUp = {})
    }
}


