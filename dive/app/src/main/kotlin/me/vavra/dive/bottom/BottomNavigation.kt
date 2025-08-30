package me.vavra.dive.bottom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import me.vavra.dive.NavDestination
import me.vavra.dive.common.ui.InlinedLoadingIndicator
import me.vavra.dive.common.ui.UserRating

@Composable
fun BottomNavigation(
    navController: NavController,
    content: @Composable ((PaddingValues) -> Unit)
) {
    val currentDestination = navController.currentBackStackEntry?.destination?.toBottomNavItem()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentDestination) { item ->
                navController.navigate(item.destination) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Dive",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = " ${currentDestination?.label?.lowercase()}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                UserMenu(navController)
            }
        },
        floatingActionButton = {
            if (currentDestination == BottomNavItem.Feed || currentDestination == BottomNavItem.Chat) {
                FloatingActionButton(onClick = {
                    if (currentDestination == BottomNavItem.Feed) {
                        navController.navigate(NavDestination.NewPost)
                    } else {
                        navController.navigate(NavDestination.NewChat)
                    }
                }) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun UserMenu(navController: NavController) {
    val viewModel = viewModel<UserViewModel>()
    val state = viewModel.state
    if (state.isLoading) {
        InlinedLoadingIndicator()
    } else {
        var expanded by remember { mutableStateOf(false) }
        Box {
            Row(
                modifier = Modifier.clickable { expanded = !expanded }
            ) {
                UserRating(
                    user = checkNotNull(state.user),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    avatarSize = 50.dp
                )
            }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.align(Alignment.BottomEnd),
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Moje hodnocení") },
                    onClick = {
                        navController.navigate(NavDestination.MyRatings)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Odhlásit") },
                    onClick = { viewModel.logOut() }
                )
            }
        }
    }
}