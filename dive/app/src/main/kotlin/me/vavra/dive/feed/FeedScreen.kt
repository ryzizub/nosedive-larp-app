package me.vavra.dive.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import me.vavra.dive.common.UserRating
import me.vavra.dive.common.theme.DiveTheme

@Composable
fun FeedScreen(state: FeedState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        items(state.posts) { post ->
            PostItem(post = post)
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
fun PostItem(post: FeedState.Post) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        UserRating(post.user, modifier = Modifier.padding(horizontal = 20.dp))
        Text(
            text = post.caption,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
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
        if (post.comments.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                post.comments.take(1).forEach { comment -> // Show only the first comment preview
                    Row {
                        Text(
                            "${comment.user.name}: ",
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
                if (post.comments.isNotEmpty()) {
                    Text(
                        "Komentáře (${post.comments.size})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp), // Increased padding
            horizontalArrangement = Arrangement.SpaceEvenly, // Distribute stars evenly
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { starIndex ->
                Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = "Ohodnotit $starIndex hvězdičkami",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(40.dp) // Larger stars
                        .clickable {
                            // TODO
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    DiveTheme {
        PostItem(post = FeedState().posts.first())
    }
}
