package me.vavra.dive.bottom

data class BottomNavigationState(
    val user: User? = null,
    val isLoading: Boolean = true
)

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
    val isNearby: Boolean
)