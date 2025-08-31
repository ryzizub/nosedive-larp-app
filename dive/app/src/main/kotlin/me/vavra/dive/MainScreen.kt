@file:OptIn(ExperimentalMaterial3Api::class)

package me.vavra.dive

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.stefanoq21.material3.navigation.ModalBottomSheetLayout
import com.stefanoq21.material3.navigation.bottomSheet
import com.stefanoq21.material3.navigation.rememberBottomSheetNavigator
import me.vavra.dive.bottom.BottomNavigation
import me.vavra.dive.chat.ChatScreen
import me.vavra.dive.chat.ConversationScreen
import me.vavra.dive.chat.NewChatScreen
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.CenteredLoadingIndicator
import me.vavra.dive.feed.CommentsScreen
import me.vavra.dive.feed.FeedScreen
import me.vavra.dive.feed.NewPostScreen
import me.vavra.dive.login.LoginScreen
import me.vavra.dive.nearby.NearbyScreen
import me.vavra.dive.rate.RateScreen
import me.vavra.dive.ratings.MyRatingsScreen


@Composable
fun MainScreen(state: MainState, onLogin: (String, String) -> Unit) {
    DiveTheme {
        when (state) {
            MainState.Loading -> LoadingScreen()
            MainState.LoggedIn -> LoggedInScreen()
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
private fun LoggedInScreen() {
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
            startDestination = NavDestination.Nearby
        ) {
            composable<NavDestination.Nearby> {
                BottomNavigation(navController) {
                    NearbyScreen(navController, Modifier.padding(it))
                }
            }
            composable<NavDestination.Feed>(deepLinks = listOf(
                navDeepLink { uriPattern = "dive://feed" }
            )) {
                BottomNavigation(navController, {
                    FeedScreen(Modifier.padding(it), navController)
                })
            }
            composable<NavDestination.Chat> {
                BottomNavigation(navController, {
                    ChatScreen(Modifier.padding(it), navController)
                })
            }
            bottomSheet<NavDestination.Conversation>(deepLinks = listOf(
                navDeepLink { uriPattern = "dive://conversation/{conversationId}" }
            )) { backStackEntry ->
                val conversationId = backStackEntry.toRoute<NavDestination.Conversation>().conversationId
                ConversationScreen(conversationId)
            }
            bottomSheet<NavDestination.Comments>(deepLinks = listOf(
                navDeepLink { uriPattern = "dive://comments/{postId}" }
            )) { backStackEntry ->
                val postId = backStackEntry.toRoute<NavDestination.Comments>().postId
                CommentsScreen(postId)
            }
            bottomSheet<NavDestination.MyRatings>(deepLinks = listOf(
                navDeepLink { uriPattern = "dive://ratings" }
            )) {
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


