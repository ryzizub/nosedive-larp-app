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
import androidx.navigation.NavController
import me.vavra.dive.NavDestination
import me.vavra.dive.bottom.User
import me.vavra.dive.common.ui.UserRating

@Composable
fun NearbyScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel = viewModel<NearbyViewModel>()
    NearbyScreenContent(
        modifier,
        viewModel.state,
        onUserSelected = {
            navController.navigate(NavDestination.Rate(it.id))
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