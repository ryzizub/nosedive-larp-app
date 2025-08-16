package me.vavra.dive

sealed class MainState {
    object Loading : MainState()
    data class LoggedIn(val user: User = User("", "", "", "", "", "", 0.0, "", "", false)): MainState()
    data class LoggedOut(val runs: List<Run>): MainState()
}

data class User(
    val id: String,
    val name: String,
    val nameVokativ: String,
    val nameAkuzativ: String,
    val nameGenitiv: String,
    val profilePictureUrl: String,
    val totalRating: Double,
    val mainRating: String,
    val detailedRating: String,
    val isVisible: Boolean
)

data class Run(
    val id: String,
    val name: String
)