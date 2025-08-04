@file:OptIn(ExperimentalMaterial3Api::class)

package me.vavra.dive

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.vavra.dive.chat.ChatScreen
import me.vavra.dive.chat.ChatState
import me.vavra.dive.chat.ConversationScreen
import me.vavra.dive.common.UserRating
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.feed.CommentsScreen
import me.vavra.dive.feed.FeedScreen
import me.vavra.dive.feed.FeedState
import me.vavra.dive.login.LoginScreen
import me.vavra.dive.nearby.NearbyScreen


@Composable
fun MainScreen(state: MainState, onLogin: (String) -> Unit, onLogout: () -> Unit) {
    DiveTheme {
        when (state) {
            MainState.Loading -> LoadingScreen()
            is MainState.LoggedIn -> LoggedInScreen(state.user, onLogout)
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

@Composable
private fun LoggedInScreen(user: User, onLoggedOut: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "nearby",
        enterTransition = {
            slideIntoContainer(
                towards = SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    ) {
        composable(
            "nearby",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }) {
            BottomNavigation(navController, user, onLoggedOut) {
                NearbyScreen(Modifier.padding(it))
            }
        }
        composable(
            "feed",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }) {
            BottomNavigation(navController, user, onLoggedOut, {
                FeedScreen(Modifier.padding(it), navController, FeedState())
            })
        }
        composable(
            "chat",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }) {
            BottomNavigation(navController, user, onLoggedOut, {
                ChatScreen(Modifier.padding(it), navController, ChatState())
            })
        }
        composable(
            route = "conversation/{partnerId}",
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId")
            val conversation = ChatState().conversations.find { it.partner.id == partnerId }
            ConversationScreen(
                navController = navController,
                conversation = checkNotNull(conversation)
            )
        }
        composable(
            route = "comments/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            val post = FeedState().posts.find { it.id == postId }
            CommentsScreen(
                navController = navController,
                post = checkNotNull(post)
            )
        }
    }
}

@Composable
private fun BottomNavigation(
    navController: NavController,
    user: User,
    onLogout: () -> Unit,
    content: @Composable ((PaddingValues) -> Unit)
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentRoute) { item ->
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
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Dive",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = " $currentRoute",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                UserMenu(user, onLogout)
            }
        },
        floatingActionButton = {
            if (currentRoute == "feed" || currentRoute == "chat") {
                FloatingActionButton(onClick = { /* TODO: Navigate to create post screen */ }) {
                    Icon(Icons.Filled.Add, "Add post")
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun UserMenu(loggedInUser: User, onLoggedOut: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            UserRating(
                user = loggedInUser,
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
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Odhlásit") },
                onClick = onLoggedOut
            )
        }
    }
}
