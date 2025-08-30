package me.vavra.dive.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import me.vavra.dive.bottom.User
import me.vavra.dive.common.Auth
import me.vavra.dive.common.Database
import me.vavra.dive.common.ui.BottomSheetTopBar
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.common.ui.MessageInput
import me.vavra.dive.common.ui.OpenAttachmentButton


@Composable
fun ConversationScreen(conversationId: String) {
    val viewModel = viewModel<ConversationViewModel>()
    LaunchedEffect(conversationId) {
        viewModel.load(conversationId)
    }
    val state = viewModel.state
    if (state.isLoading) {
        CenteredLoadingIndicator()
    } else {
        ConversationScreenContent(state, onSendMessage = { text, attachmentUrl ->
            viewModel.sendMessage(text, attachmentUrl)
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConversationScreenContent(
    state: ConversationState,
    onSendMessage: (String, Uri?) -> Unit
) {
    val listState = rememberLazyListState()
    val conversation = checkNotNull(state.conversation)
    LaunchedEffect(conversation.messages.size, WindowInsets.isImeVisible) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            BottomSheetTopBar(conversation.partner.name + " (" + conversation.partner.mainRating + ")")
        },
        bottomBar = {
            MessageInput("Napiš zprávu", state.sendingMessage, onSendMessage)
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversation.messages) { message ->
                MessageBubble(
                    message = message,
                    partner = conversation.partner,
                    isMine = message.author.id == Auth.getUserId()
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Database.Message,
    partner: User,
    isMine: Boolean
) {
    val horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    val bubbleColor =
        if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isMine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.8f), // Max width for a bubble row
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMine) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(partner.profilePictureUrl)
                        .crossfade(true)
                        .transformations(CircleCropTransformation())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                    OpenAttachmentButton(message.attachmentUrl, tonal = false)
                }
            }
        }
    }
}