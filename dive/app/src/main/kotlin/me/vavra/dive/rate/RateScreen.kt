package me.vavra.dive.rate

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.vavra.dive.NavDestination
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.StarRating

@Composable
fun RateScreen(
    navController: NavController,
    state: RateState
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    swipeOffset += dragAmount
                    if (swipeOffset > 150) {
                        navController.popBackStack()
                    } else if (swipeOffset < -150) {
                        navController.navigate(NavDestination.Rated(state.ratedUser.id))
                    }
                }

            }) {
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
