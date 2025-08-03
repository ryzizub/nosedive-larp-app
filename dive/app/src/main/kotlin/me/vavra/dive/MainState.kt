package me.vavra.dive

import me.vavra.dive.nearby.User

sealed class MainState {
    object Loading : MainState()
    data class LoggedIn(val user: User = User("", "", "", "", "", "", 0.0, "", "", false)): MainState()
    object LoggedOut: MainState()
}