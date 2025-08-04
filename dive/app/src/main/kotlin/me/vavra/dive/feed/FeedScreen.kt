package me.vavra.dive.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.StarRating
import me.vavra.dive.common.ui.UserRating

@Composable
fun FeedScreen(modifier: Modifier, navController: NavHostController, state: FeedState) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.posts) { post ->
            PostItem(post = post) {
                navController.navigate("comments/${post.id}")
            }
        }
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PostItem(post: FeedState.Post, onPostClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.clickable {
            onPostClicked()
        }) {
            Post(post)
            if (post.comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    post.comments.take(1)
                        .forEach { comment -> // Show only the first comment preview
                            Row {
                                Text(
                                    "${comment.user.name} (${comment.user.mainRating}): ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    comment.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    val remainingCommentCount = post.comments.size - 1
                    if (remainingCommentCount > 0) {
                        val text = when (remainingCommentCount) {
                            1 -> "další komentář"
                            2, 3, 4 -> "dalších komentáře"
                            else -> "dalších komentářů"
                        }
                        Text(
                            "$remainingCommentCount $text",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        StarRating(modifier = Modifier.align(CenterHorizontally))
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ColumnScope.Post(post: FeedState.Post) {
    Spacer(modifier = Modifier.height(8.dp))
    UserRating(post.user, modifier = Modifier.padding(horizontal = 20.dp))
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = post.caption,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )

    // Post Image
    SubcomposeAsyncImage(
        model = post.imageUrl,
        contentDescription = "Obrázek příspěvku od ${post.user.name}",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square aspect ratio for main image
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop
    ) {
        val painterState = painter.state
        if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Error) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SubcomposeAsyncImageContent()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    DiveTheme {
        PostItem(post = FeedState().posts.first(), {})
    }
}
