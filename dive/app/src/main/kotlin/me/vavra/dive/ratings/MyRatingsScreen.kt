package me.vavra.dive.ratings // Changed package name

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.theme.Rate
import me.vavra.dive.common.ui.Avatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRatingsScreen(
    state: MyRatingsState
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Obdržená hodnocení", "Odeslaná hodnocení")

    Scaffold(
        topBar = {
            val activity = LocalActivity.current as ComponentActivity?
            TopAppBar(
                title = { Text("Moje hodnocení") },
                navigationIcon = {
                    IconButton(onClick = { activity?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Zpět")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (selectedTabIndex) {
                0 -> RatingsList(ratings = state.ratingsOfMe)
                1 -> RatingsList(ratings = state.ratingsByMe)
            }
        }
    }
}

@Composable
fun RatingsList(ratings: List<MyRatingsState.Rating>) {
    if (ratings.isEmpty()) {
        Text(
            text = "Zatím žádná hodnocení.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .wrapContentSize(Alignment.Center)
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(ratings) { rating ->
            RatingListItem(rating = rating)
        }
    }
}

@Composable
fun RatingListItem(rating: MyRatingsState.Rating) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(rating.user, 40.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = rating.user.name+" (${rating.user.mainRating})")
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { starIndex ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = if (starIndex <= rating.stars) "Star Filled" else "Star Empty",
                        tint = if (starIndex <= rating.stars) Rate else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(
            text = formatDate(rating.timestamp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    return format.format(date)
}

@Preview(showBackground = true)
@Composable
fun MyRatingsScreenPreview() {
    DiveTheme {
        MyRatingsScreen(
            state = MyRatingsState()
        )
    }
}

