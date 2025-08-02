package me.vavra.dive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.theme.Nosedive1
import me.vavra.dive.common.theme.Nosedive2

@OptIn(ExperimentalMaterial3Api::class) // Added this annotation
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val ratingOnBackCallback = object : OnBackPressedCallback(enabled = false) {
        override fun handleOnBackPressed() {
            viewModel.closeRating()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        onBackPressedDispatcher.addCallback(this, ratingOnBackCallback)
        setContent {
            DiveTheme {
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(Nosedive1, Nosedive2)
                                        )
                                    )
                            ) {
                                val state = viewModel.state
                                if (state.loggedInUser == null) {
                                    LoginScreen(state.loggingIn, onLogin = { viewModel.login(it) })
                                } else {
                                    if (state.rating == null) {
                                        MainScreen(state.nearbyUsers, state.loggedInUser, onUserSelected = {
                                            viewModel.selectUser(it)
                                        }, onLoggedOut = { viewModel.logOut() })
                                        ratingOnBackCallback.isEnabled = false
                                    } else {
                                        if (state.rating.success) {
                                            RatedScreen(state.rating, onClose = { viewModel.closeRating() })
                                        } else if (state.rating.fail) {
                                            RatingFailedScreen(
                                                state.rating,
                                                onClose = { viewModel.closeRating() })
                                        } else {
                                            RateScreen(
                                                state.loggedInUser,
                                                state.rating,
                                                onRatingChanged = { viewModel.changeRating(it) },
                                                onClose = { viewModel.closeRating() },
                                                onSend = { viewModel.sendRating() })
                                        }
                                        ratingOnBackCallback.isEnabled = true
                                    }
                                }
                            }
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
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean ->

    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
