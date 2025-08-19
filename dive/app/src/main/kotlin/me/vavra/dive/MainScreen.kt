@file:OptIn(ExperimentalMaterial3Api::class)

package me.vavra.dive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.stefanoq21.material3.navigation.ModalBottomSheetLayout
import com.stefanoq21.material3.navigation.bottomSheet
import com.stefanoq21.material3.navigation.rememberBottomSheetNavigator
import me.vavra.dive.chat.ChatScreen
import me.vavra.dive.chat.ChatState
import me.vavra.dive.chat.ConversationScreen
import me.vavra.dive.chat.NewChatScreen
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.common.ui.UserRating
import me.vavra.dive.feed.CommentsScreen
import me.vavra.dive.feed.FeedScreen
import me.vavra.dive.feed.FeedState
import me.vavra.dive.feed.NewPostScreen
import me.vavra.dive.login.LoginScreen
import me.vavra.dive.nearby.NearbyScreen
import me.vavra.dive.rate.RateScreen
import me.vavra.dive.ratings.MyRatingsScreen


@Composable
fun MainScreen(state: MainState, onLogin: (String, String) -> Unit, onLogout: () -> Unit) {
    DiveTheme {
        when (state) {
            MainState.Loading -> LoadingScreen()
            is MainState.LoggedIn -> LoggedInScreen(state.user, onLogout)
            is MainState.LoggedOut -> LoginScreen(state, onLogin)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingScreen() {
    CenteredLoadingIndicator()
}

@Composable
private fun LoggedInScreen(user: User, onLoggedOut: () -> Unit) {
    val bottomSheetNavigator = rememberBottomSheetNavigator(skipPartiallyExpanded = true)
    val navController = rememberNavController(bottomSheetNavigator)
    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        bottomSheetNavigator = bottomSheetNavigator,
        sheetModifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        dragHandle = {}
    ) {
        NavHost(
            navController = navController,
            startDestination = NavDestination.Nearby,
        ) {
            composable<NavDestination.Nearby> {
                BottomNavigation(navController, user, onLoggedOut) {
                    NearbyScreen(navController, Modifier.padding(it))
                }
            }
            composable<NavDestination.Feed> {
                BottomNavigation(navController, user, onLoggedOut, {
                    FeedScreen(Modifier.padding(it), navController, FeedState())
                })
            }
            composable<NavDestination.Chat> {
                BottomNavigation(navController, user, onLoggedOut, {
                    ChatScreen(Modifier.padding(it), navController, ChatState())
                })
            }
            bottomSheet<NavDestination.Conversation> { backStackEntry ->
                val partnerId = backStackEntry.toRoute<NavDestination.Conversation>().partnerId
                val conversation = ChatState().conversations.find { it.partner.id == partnerId }
                ConversationScreen(
                    conversation = checkNotNull(conversation)
                )
            }
            bottomSheet<NavDestination.Comments> { backStackEntry ->
                val postId = backStackEntry.toRoute<NavDestination.Comments>().postId
                val post = FeedState().posts.find { it.id == postId }
                CommentsScreen(
                    post = checkNotNull(post)
                )
            }
            bottomSheet<NavDestination.MyRatings> {
                MyRatingsScreen()
            }
            bottomSheet<NavDestination.Rate> { backStackEntry ->
                val userId = backStackEntry.toRoute<NavDestination.Rate>().userId
                RateScreen(userId)
            }
            bottomSheet<NavDestination.NewPost> {
                NewPostScreen()
            }
            bottomSheet<NavDestination.NewChat> {
                NewChatScreen(navController)
            }
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
                UserMenu(navController, user, onLogout)
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
private fun UserMenu(navController: NavController, loggedInUser: User, onLoggedOut: () -> Unit) {
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
                onClick = {
                    navController.navigate(NavDestination.MyRatings)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Odhlásit") },
                onClick = onLoggedOut
            )
        }
    }
}
