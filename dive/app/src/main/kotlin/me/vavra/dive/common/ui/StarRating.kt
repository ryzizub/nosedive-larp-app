package me.vavra.dive.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import me.vavra.dive.common.theme.Rate

@Composable
fun StarRating(modifier: Modifier = Modifier) {
    var stars: Float by remember { mutableFloatStateOf(0f) }
    RatingBar(
        value = stars,
        style = RatingBarStyle.Stroke(activeColor = Rate, width = 3f),
        onValueChange = { stars = it },
        size = 46.dp,
        spaceBetween = 6.dp,
        onRatingChanged = { /*TODO*/ },
        modifier = modifier
    )
}