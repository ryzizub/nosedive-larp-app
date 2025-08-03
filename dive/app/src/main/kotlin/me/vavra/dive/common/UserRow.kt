package me.vavra.dive.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import me.vavra.dive.User

@Composable
fun UserRating(user: User, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profilePictureUrl)
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
            Text(text = user.name, style = MaterialTheme.typography.titleMedium)
            Row {
                Text(text = user.mainRating, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = user.detailedRating,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(bottom = 2.dp, start = 1.dp)
                )
            }
        }
    }
}