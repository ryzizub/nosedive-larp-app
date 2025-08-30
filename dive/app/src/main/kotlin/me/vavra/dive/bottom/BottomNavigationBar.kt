package me.vavra.dive.bottom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import me.vavra.dive.NavDestination

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val destination: NavDestination
) {
    Nearby("Nearby", Icons.Default.Place, NavDestination.Nearby),
    Feed("Feed", Icons.AutoMirrored.Default.List, NavDestination.Feed),
    Chat("Chat", Icons.Default.ChatBubble, NavDestination.Chat)
}

@Composable
fun BottomNavigationBar(
    currentTab: BottomNavItem?,
    onTabSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = item == currentTab,
                onClick = {
                    onTabSelected(item)
                }
            )
        }
    }
}

fun androidx.navigation.NavDestination.toBottomNavItem(): BottomNavItem {
    return BottomNavItem.entries.firstOrNull { entry -> this.hierarchy.any { it.hasRoute(entry.destination::class) } } ?: BottomNavItem.Nearby
}
