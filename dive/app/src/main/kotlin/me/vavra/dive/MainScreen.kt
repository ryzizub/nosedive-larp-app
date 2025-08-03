package me.vavra.dive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.vavra.dive.chat.ChatScreen
import me.vavra.dive.common.UserRating
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.feed.FeedScreen
import me.vavra.dive.feed.FeedState
import me.vavra.dive.nearby.NearbyScreen

@Composable
fun MainScreen(state: MainState, onLogin: (String) -> Unit, onLogout: () -> Unit) {
    DiveTheme {
        when (state) {
            MainState.Loading -> LoadingScreen()
            is MainState.LoggedIn -> MainNavigation(state.user, onLogout)
            MainState.LoggedOut -> LoginScreen(onLogin)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigation(user: User, onLoggedOut: ()-> Unit) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(BottomNavItem.Nearby) }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedItem) { item ->
                selectedItem = item
                navController.navigate(item.route) {
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
                    .padding(horizontal = 20.dp)
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            ) {
                Text(
                    text = "Dive",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = " ${selectedItem.label.lowercase()}", style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                LogoutDropDown(user, onLoggedOut)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "nearby",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("nearby") {
                NearbyScreen()
            }
            composable("feed") {
                FeedScreen(FeedState())
            }
            composable("chat") {
                ChatScreen()
            }
        }
    }
}

@Composable
private fun LogoutDropDown(loggedInUser: User, onLoggedOut: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box() {
        Row(
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            UserRating(user = loggedInUser)
        }
        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.align(Alignment.BottomEnd),
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Moje hodnocení") },
                onClick = {  }
            )
            DropdownMenuItem(
                text = { Text("Odhlásit") },
                onClick = onLoggedOut
            )
        }
    }
}