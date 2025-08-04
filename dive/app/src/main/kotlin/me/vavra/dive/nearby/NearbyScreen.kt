package me.vavra.dive.nearby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vavra.dive.User
import me.vavra.dive.common.UserRating

@Composable
fun NearbyScreen(modifier: Modifier = Modifier) {
    val viewModel = viewModel<NearbyViewModel>()
    NearbyScreenContent(
        modifier,
        viewModel.state,
        onUserSelected = { // TODO
        })
}

@Composable
private fun NearbyScreenContent(
    modifier: Modifier,
    state: NearbyState,
    onUserSelected: (User) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(state.nearbyUsers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserSelected(it) }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                UserRating(it)
            }
        }
    }
}