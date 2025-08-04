package me.vavra.dive.feed

import me.vavra.dive.User

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
    val posts: List<Post> = listOf(
        Post(
            id = "post2",
            user = sampleUsers[1], // Naomi
            imageUrl = "https://picsum.photos/seed/wedding_main/1080/1080",
            caption = "Cítím se TAK vděčná za všechny, kdo přišli na mou #svatbusnů! Byla to rozhodně událost 4.9+! Tolik lásky! 🥰💍",
            comments = listOf(
                Comment("comment2_1", sampleUsers[0], "OMG Naomi, bylo to TO NEJLEPŠÍ! Tak zasloužené! 💖💖💖", System.currentTimeMillis() - 50000),
                Comment("comment2_2", sampleUsers[1], "Bezchybná oslava lásky. Pět hvězd po všech stránkách! 🌟")
            ),
        ), Post(
            id = "post1",
            user = sampleUsers[0],
            imageUrl = "https://picsum.photos/seed/coffee_main/1080/1080",
            caption = "Naprosto jsem si zamilovala ranní kávu! ☕️ Takový #požehnaný start do #perfektníhodne! Posílám 5hvězdičkovou energii všem! ✨",
            comments = listOf(
                Comment("comment1_1", sampleUsers[1], "Vypadá to úžasně, Lacie! Mám za tebe takovou radost! ❤️", System.currentTimeMillis() - 100000),
                Comment("comment1_2", sampleUsers[2], "Opravdová inspirace. Tvá pozitivita je nakažlivá! ⭐⭐⭐⭐⭐")
            ),
        ),
        Post(
            id = "post3",
            user = sampleUsers[2], // Chester (lower rating)
            imageUrl = "https://picsum.photos/seed/lunch_main/1080/1080",
            caption = "Můj dnešní oběd. Byl tak nějak v pohodě, myslím. Snažím se zůstat pozitivní! #oběd #jídlo",
            comments = listOf(
                Comment("comment3_1", sampleUsers[0], "Drž se, Chestere! Každý den je nová příležitost pro 5hvězdičkový zážitek! Posílám pozitivní energii! 😊", System.currentTimeMillis() - 20000)
            ),
        )
    )
) {
    data class Comment(
        val id: String,
        val user: User,
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class Post(
        val id: String,
        val user: User,
        val imageUrl: String,
        val caption: String,
        val comments: List<Comment>
    )
}
