package me.vavra.dive.nearby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vavra.dive.User
import me.vavra.dive.common.UserRating

@Composable
fun NearbyScreen() {
    val viewModel = viewModel<NearbyViewModel>()
    NearbyScreenContent(
        viewModel.state,
        onUserSelected = { // TODO
        })
}

@Composable
private fun NearbyScreenContent(
    state: NearbyState,
    onUserSelected: (User) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(all = 20.dp)
    ) {
        items(state.nearbyUsers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserSelected(it) }
            ) {
                UserRating(it)
            }
        }
    }
}