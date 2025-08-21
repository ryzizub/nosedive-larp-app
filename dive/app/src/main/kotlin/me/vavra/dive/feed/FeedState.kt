package me.vavra.dive.feed

import me.vavra.dive.User
import me.vavra.dive.common.Database

val sampleUsers = listOf(
    User(
        "sylva",
        "Sylva Malá",
        "Sylvo",
        "Sylvu",
        "Sylvy",
        "https://firebasestorage.googleapis.com/v0/b/nosedive-larp.appspot.com/o/profile_pics%2F6_sylva.jpg?alt=media&token=afb9a5b0-fada-4b34-b5fe-8c219434b5a2",
        0.0,
        "4.1",
        "85",
        true
    ),
    User(
        "nina",
        "Nina Králová",
        "Nino",
        "Ninu",
        "Niny",
        "https://firebasestorage.googleapis.com/v0/b/nosedive-larp.appspot.com/o/profile_pics%2F6_nina_2.jpg?alt=media&token=74324c28-4c65-44ac-b336-b8fa4cee75fa",
        0.0,
        "4.5",
        "66",
        true
    ),
    User(
        "mirek",
        "Mirek Pospíšil",
        "Mirku",
        "Mirka",
        "Mirka",
        "https://firebasestorage.googleapis.com/v0/b/nosedive-larp.appspot.com/o/profile_pics%2F6_mirek.jpg?alt=media&token=d37e38b6-c8a5-4f25-bf58-42a0f2aec63b",
        0.0,
        "3.7",
        "92",
        true
    ),
)

data class FeedState(
    val posts: List<Post> = listOf(),
    val isLoading: Boolean = true
) {

    data class Post(
        val id: String,
        val user: User,
        val imageUrl: String,
        val text: String,
        val stars: Int,
        val rated: Boolean,
        val comments: List<Database.Message>
    )
}
