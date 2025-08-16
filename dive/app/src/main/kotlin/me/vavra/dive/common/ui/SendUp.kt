package me.vavra.dive.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SendUp(modifier: Modifier = Modifier, canSend: Boolean, sending: Boolean, onSend: () -> Unit) {
    if (canSend && !sending) {
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current // Get screen configuration
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        val listItemOffsetY = remember { Animatable(0f) }
        // Threshold to trigger send action: 60% of screen height (negative for upward drag)
        val sendThresholdPx = -screenHeightPx * 0.5f
        ListItem(
            headlineContent = {
                Text(
                    "Odeslat",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            supportingContent = {
                Text(
                    "táhnutím nahoru",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Odeslat"
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Odeslat"
                )
            },
            modifier = modifier
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
                .height(64.dp)
        )
    } else if (sending) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            InlinedLoadingIndicator()
            Spacer(modifier = Modifier.width(16.dp))
            Text("Odesílám")
        }
    } else {
        Spacer(modifier = Modifier.height(64.dp))
    }
}