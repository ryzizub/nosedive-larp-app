package me.vavra.dive.rate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape // Added import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.StarRating
import me.vavra.dive.feed.sampleUsers

@Composable
fun RateScreen(
    state: RateState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 20.dp)) {
            Text(
                state.currentUser.nameVokativ + ",",
                modifier = Modifier.align(CenterHorizontally),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                "zde můžeš ohodnotit",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Avatar(state.ratedUser, 200.dp, modifier = Modifier.align(CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                state.ratedUser.nameAkuzativ,
                modifier = Modifier.align(CenterHorizontally),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            StarRating(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(32.dp))
            ListItem(
                headlineContent = { Text("Odeslat", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                supportingContent = { Text("táhnutím nahoru", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                leadingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Odeslat") },
                trailingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Odeslat") },
                modifier = Modifier.border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                )
            )
        }
    }
}

@Preview
@Composable
fun RateScreenPreview() {
    DiveTheme {
        RateScreen(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1]
            )
        )
    }
}
