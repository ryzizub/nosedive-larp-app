package me.vavra.dive.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import me.vavra.dive.common.theme.Rate

@Composable
fun StarRating(modifier: Modifier = Modifier, stars: Int, interactive: Boolean, onRatingChanged: (Int) -> Unit) {
    RatingBar(
        value = stars.toFloat(),
        style = RatingBarStyle.Stroke(activeColor = Rate, width = 3f),
        size = 46.dp,
        spaceBetween = 6.dp,
        onRatingChanged = { onRatingChanged(it.toInt()) },
        onValueChange = { },
        modifier = modifier,
    )
}