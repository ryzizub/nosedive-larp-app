package me.vavra.dive.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import me.vavra.dive.NavDestination
import me.vavra.dive.User
import me.vavra.dive.common.ui.BottomSheetTopBar
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.common.ui.UserRating

@Composable
fun NewChatScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel = viewModel<NewChatViewModel>()
    val state = viewModel.state
    if (state.isLoading) {
        CenteredLoadingIndicator()
    } else {
        NewChatScreenContent(
            modifier,
            viewModel.state,
            onUserSelected = { user ->
                viewModel.selectUser(user.id) {
                    navController.navigate(NavDestination.Conversation(it))
                }
            })
    }
}

@Composable
private fun NewChatScreenContent(
    modifier: Modifier,
    state: NewChatState,
    onUserSelected: (User) -> Unit
) {
    Scaffold(
        topBar = {
            BottomSheetTopBar("Nový chat")
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.padding(paddingValues)
        ) {
            items(state.users) {
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
}