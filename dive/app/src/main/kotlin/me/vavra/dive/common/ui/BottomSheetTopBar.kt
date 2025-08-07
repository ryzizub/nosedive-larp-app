package me.vavra.dive.common.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTopBar(title: String) {
    val activity = LocalActivity.current as ComponentActivity?
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back"
                )
            }
        }
    )
}