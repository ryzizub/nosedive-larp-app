package me.vavra.dive.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import me.vavra.dive.NavDestination
import me.vavra.dive.User
import me.vavra.dive.common.ui.CenteredLoadingIndicator

@Composable
fun ChatScreen(modifier: Modifier, navController: NavController) {
    val viewModel = viewModel<ChatViewModel>()
    val state = viewModel.state
    if (state.isLoading) {
        CenteredLoadingIndicator()
    } else {
        ChatScreenContent(
            modifier,
            state,
            onConversationSelected = {
                navController.navigate(NavDestination.Conversation(it.id))
            })
    }
}

@Composable
private fun ChatScreenContent(
    modifier: Modifier,
    state: ChatState,
    onConversationSelected: (ChatState.Conversation) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(state.conversations) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConversationSelected(it) }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Conversation(it)
            }
        }
    }
}

@Composable
private fun RowScope.Conversation(conversation: ChatState.Conversation) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(conversation.partner.profilePictureUrl)
            .crossfade(true)
            .transformations(CircleCropTransformation())
            .build(),
        contentDescription = null,
        modifier = Modifier.size(60.dp)
    )
    Column(
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(start = 16.dp)
    ) {
        Text(
            text = conversation.partner.name + " (" + conversation.partner.mainRating + ")",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (conversation.unread) FontWeight.Bold else null
        )
        if (conversation.messages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.messages.last().text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (conversation.unread) FontWeight.Bold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ChatScreenContentPreview() {
    ChatScreenContent(
        modifier = Modifier,
        state = ChatState()
    ) {}
}