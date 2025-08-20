package me.vavra.dive.rate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.Avatar
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.common.ui.SendUp
import me.vavra.dive.common.ui.StarRating
import me.vavra.dive.feed.sampleUsers

@Composable
fun RateScreen(userId: String) {
    val viewModel = viewModel<RateViewModel>()
    LaunchedEffect(Unit) {
        viewModel.load(userId)
    }
    RateScreenContent(viewModel.state, onSend = {
        viewModel.sendRating()
    }, onRatingChanged = {
        viewModel.changeStars(it)
    })
}

@Composable
private fun RateScreenContent(
    state: RateState,
    onSend: () -> Unit = {},
    onRatingChanged: (Int) -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (state.currentUser == null || state.ratedUser == null) {
            CenteredLoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
            ) {
                when (state.progress) {
                    RateState.Progress.INITIAL, RateState.Progress.SENDING -> {
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
                    }

                    RateState.Progress.SUCCESS, RateState.Progress.FAIL -> {
                        Text(
                            "Hodnocení",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(CenterHorizontally)
                        )
                        Text(
                            state.ratedUser.nameGenitiv,
                            modifier = Modifier.align(CenterHorizontally),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Avatar(state.ratedUser, 200.dp, modifier = Modifier.align(CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                when (state.progress) {
                    RateState.Progress.INITIAL, RateState.Progress.SENDING -> {
                        Text(
                            state.ratedUser.nameAkuzativ,
                            modifier = Modifier.align(CenterHorizontally),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    RateState.Progress.SUCCESS -> {
                        Text(
                            "odesláno",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(CenterHorizontally)
                        )
                    }

                    RateState.Progress.FAIL -> {
                        Text(
                            "se nepodařilo odeslat.",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Zkontrolujte připojení.",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(CenterHorizontally)
                        )
                    }
                }

                if (state.progress != RateState.Progress.FAIL) {
                    Spacer(modifier = Modifier.height(32.dp))
                    StarRating(
                        modifier = Modifier.align(CenterHorizontally),
                        stars = state.stars,
                        interactive = state.progress == RateState.Progress.INITIAL,
                        onRatingChanged = onRatingChanged
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                SendUp(
                    canSend = state.progress == RateState.Progress.INITIAL && state.stars > 0,
                    sending = state.progress == RateState.Progress.SENDING,
                    onSend = onSend
                )
            }
        }
    }
}

@Preview
@Composable
fun RateScreenInitialPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1]
            )
        )
    }
}

@Preview
@Composable
fun RateScreenInitialStarsPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1],
                stars = 3
            )
        )
    }
}

@Preview
@Composable
fun RateScreenSendingPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1],
                progress = RateState.Progress.SENDING
            )
        )
    }
}

@Preview
@Composable
fun RateScreenSuccessPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1],
                progress = RateState.Progress.SUCCESS
            )
        )
    }
}

@Preview
@Composable
fun RateScreenFailPreview() {
    DiveTheme {
        RateScreenContent(
            state = RateState(
                currentUser = sampleUsers[0],
                ratedUser = sampleUsers[1],
                progress = RateState.Progress.FAIL
            )
        )
    }
}
