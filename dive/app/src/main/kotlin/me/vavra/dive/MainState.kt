package me.vavra.dive

sealed class MainState {
    object Loading : MainState()
    object LoggedIn : MainState()
    data class LoggedOut(val runs: List<Run>) : MainState()
}

data class Run(
    val id: String,
    val name: String
)