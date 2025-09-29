package me.vavra.dive.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import me.vavra.dive.NavDestination
import me.vavra.dive.common.Auth
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.common.ui.StarRating
import me.vavra.dive.common.ui.UserRating

@Composable
fun FeedScreen(modifier: Modifier, navController: NavHostController) {
    val viewModel = viewModel<FeedViewModel>()
    val state = viewModel.state
    if (state.isLoading) {
        CenteredLoadingIndicator()
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(state.posts) { post ->
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                PostItem(post = post, onPostClicked = {
                    navController.navigate(NavDestination.Comments(post.id))
                }, onPostRated = {
                    viewModel.rate(post.id, it)
                })
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun PostItem(post: FeedState.Post, onPostClicked: () -> Unit, onPostRated: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RectangleShape,
        onClick = onPostClicked
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Post(post)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                post.comments.take(1)
                    .forEach { comment ->                 // Show first comment if exists

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                "${comment.author.name} (${comment.author.mainRating})",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                comment.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }


                val remainingCommentCount = post.comments.size - 1
                val text = when (remainingCommentCount) {
                    -1, 0 -> "Přidat komentář"
                    1 -> "$remainingCommentCount další komentář"
                    2, 3, 4 -> "$remainingCommentCount další komentáře"
                    else -> "$remainingCommentCount dalších komentářů"
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        if (post.user.id != Auth.getUserId()) {
            Spacer(modifier = Modifier.height(8.dp))
            val interactive = !post.rated
            StarRating(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(vertical = 20.dp),
                stars = post.stars,
                interactive = interactive,
                onPostRated
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ColumnScope.Post(post: FeedState.Post) {
    Spacer(modifier = Modifier.height(12.dp))
    UserRating(post.user, modifier = Modifier.padding(horizontal = 16.dp))

    if (post.text.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = post.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }

    // Post Image
    if (post.imageUrl != null) {
        Spacer(modifier = Modifier.height(12.dp))
        SubcomposeAsyncImage(
            model = post.imageUrl,
            contentDescription = "Obrázek příspěvku od ${post.user.name}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        ) {
            val painterState = painter.state
            if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Error) {
                CenteredLoadingIndicator()
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    DiveTheme {
        PostItem(post = FeedState().posts.first(), {}, { _ -> })
    }
}
