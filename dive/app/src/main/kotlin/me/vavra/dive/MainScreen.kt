package me.vavra.dive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.nearby.NearbyScreen

@Composable
fun MainScreen(state: MainState, onLogin: (String) -> Unit) {
    DiveTheme {
        when (state) {
            MainState.LOADING -> LoadingScreen()
            MainState.LOGGED_IN -> MainNavigation()
            MainState.LOGGED_OUT -> LoginScreen(onLogin)
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

@Composable
private fun MainNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Feed Screen")
                }
            }
            composable("chat") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chat Screen")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenLoadingPreview() {
    MainScreen(state = MainState.LOADING, onLogin = {})
}

@Preview(showBackground = true)
@Composable
fun MainScreenLoggedInPreview() {
    MainScreen(state = MainState.LOGGED_IN, onLogin = {})
}

@Preview(showBackground = true)
@Composable
fun MainScreenLoggedOutPreview() {
    MainScreen(state = MainState.LOGGED_OUT, onLogin = {})
}