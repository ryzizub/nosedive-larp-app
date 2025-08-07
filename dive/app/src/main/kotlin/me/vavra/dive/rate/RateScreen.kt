package me.vavra.dive.rate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.StarRating
import me.vavra.dive.feed.sampleUsers
import kotlin.math.roundToInt

@Composable
fun RateScreen() {
    val viewModel = viewModel<RateViewModel>()
    RateScreenContent(viewModel.state, onSend = {
        viewModel.sendRating()
    })
}

@Composable
private fun RateScreenContent(
    state: RateState,
    onSend: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current // Get screen configuration
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val listItemOffsetY = remember { Animatable(0f) }
    // Threshold to trigger send action: 60% of screen height (negative for upward drag)
    val sendThresholdPx = -screenHeightPx * 0.5f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
        ) {
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
            Spacer(modifier = Modifier.height(32.dp))

            when (state.progress) {
                RateState.Progress.SENDING -> {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Odesílám hodnocení")
                    }
                }

                RateState.Progress.INITIAL -> {
                    ListItem(
                        headlineContent = { Text("Odeslat", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        supportingContent = { Text("táhnutím nahoru", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        leadingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Odeslat") },
                        trailingContent = { Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Odeslat") },
                        modifier = Modifier
                            .offset { IntOffset(0, listItemOffsetY.value.roundToInt()) }
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    scope.launch {
                                        // Allow dragging upwards without a hard limit,
                                        // but not below its original position (0f).
                                        val newOffset = (listItemOffsetY.value + delta).coerceAtMost(0f)
                                        listItemOffsetY.snapTo(newOffset)
                                    }
                                },
                                onDragStopped = { velocity ->
                                    scope.launch {
                                        val currentOffset = listItemOffsetY.value
                                        // If dragged beyond screen height based threshold, or with significant upward velocity
                                        if (currentOffset < sendThresholdPx || velocity < -400f) { // -400f is an example velocity threshold
                                            onSend()
                                        } else {
                                            // Snap back to original position if not dragged enough
                                            listItemOffsetY.animateTo(0f, animationSpec = tween(300))
                                        }
                                    }
                                }
                            )
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                else -> { /* nothing */
                }
            }
        }
    }
}

@Preview
@Composable
fun RateScreenPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1]
            ), onSend = {}
        )
    }
}
