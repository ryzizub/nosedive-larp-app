package me.vavra.dive.feed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.BottomSheetTopBar
import me.vavra.dive.common.ui.MessageInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommentsScreen(
    post: FeedState.Post
) {
    val listState = rememberLazyListState()
    LaunchedEffect(post.comments.size, WindowInsets.isImeVisible) {
        if (post.comments.isNotEmpty()) {
            listState.animateScrollToItem(post.comments.size)
        }
    }

    Scaffold(
        topBar = {
            BottomSheetTopBar("Komentáře")
        },
        bottomBar = {
            MessageInput("Napiš komentář")
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            item {
                Column {
                    Post(post)
                }
            }

            items(post.comments) { comment ->
                CommentItem(comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: FeedState.Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(comment.user, 40.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = comment.user.name+" ("+comment.user.mainRating+")",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostDetailScreenPreview() {
    DiveTheme {
        CommentsScreen(
            post = FeedState().posts.first()
        )
    }
}
