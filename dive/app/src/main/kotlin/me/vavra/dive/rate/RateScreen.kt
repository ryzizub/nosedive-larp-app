package me.vavra.dive.rate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.StarRating

@Composable
fun RateScreen(
    navController: NavController,
    state: RateState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.align(Alignment.Center)) {
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
            StarRating(modifier = Modifier.align(CenterHorizontally))
        }
    }
}
