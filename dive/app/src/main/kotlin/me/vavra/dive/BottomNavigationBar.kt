package me.vavra.dive

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Email // Changed from ChatBubble
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    Nearby("Nearby", Icons.Default.Place, "nearby"),
    Feed("Feed", Icons.AutoMirrored.Default.List, "feed"),
    Chat("Chat", Icons.Default.ChatBubble, "chat")
}

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selectedItem == item,
                onClick = {
                    onTabSelected(item)
                }
            )
        }
    }
}
