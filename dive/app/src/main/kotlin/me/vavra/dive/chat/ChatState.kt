package me.vavra.dive.chat

import me.vavra.dive.User
import me.vavra.dive.feed.sampleUsers

data class ChatState(
    val conversations: List<Conversation> = listOf(
        // Conversation 1: sampleUsers[0] (Sylva) and sampleUsers[1] (Nina)
        Conversation(
            partner = sampleUsers[1], // Nina Králová
            unread = true,
            messages = listOf(
                Message(
                    isMine = false,
                    text = "Ahoj Sylvo! ✨ Tvoje včerejší párty byla naprosto 5hvězdičková! Úžasná atmosféra!"
                ),
                Message(
                    isMine = true,
                    text = "Nino, děkuji! Jsem tak ráda, že sis to užila. Tvoje pozitivní energie byla nakažlivá! 😊"
                ),
                Message(
                    isMine = false,
                    text = "Musíme to brzy zopakovat! Možná nějaký exkluzivní brunch pro 4.5+? 😉 Potřebuji si vylepšit skóre po tom incidentu s číšníkem..."
                ),
                Message(
                    isMine = true,
                    text = "Skvělý nápad! Dám vědět, až najdu perfektní místo. Jen ty nejlepší vibrace! ⭐"
                )
            )
        ),
        // Conversation 2: sampleUsers[0] (Sylva) and sampleUsers[2] (Mirek)
        Conversation(
            partner = sampleUsers[2], // Mirek Pospíšil
            unread = false,
            messages = listOf(
                Message(
                    isMine = false,
                    text = "Čau Sylvo. Díky za pozvání včera. Bylo to... zajímavé."
                ),
                Message(
                    isMine = true,
                    text = "Ahoj Mirku! Jsem ráda, že jsi dorazil. Doufám, že ses bavil?"
                ),
                Message(
                    isMine = false,
                    text = "No, abych byl upřímný, ta hudba byla trochu moc nahlas pro můj vkus. A ty jednohubky... nebyly zrovna můj šálek čaje. Ale jinak fajn, no."
                ),
                Message(
                    isMine = true,
                    text = "Ach tak. No, snažila jsem se vyhovět všem. Každopádně díky za upřímnost, Mirku. Toho si cením. (I když by to mohlo být 3.5hvězdičkové hodnocení upřímnosti 😉)"
                ),
                Message(
                    isMine = false,
                    text = "Jo, promiň, jestli to znělo drsně. Jen říkám, jak to vidím. Ne každý den je pětihvězdičkový, že? Drž se."
                )
            )
        )
    )
) {
    data class Conversation(
        val partner: User,
        val messages: List<Message>,
        val unread: Boolean
    )

    data class Message(val text: String, val isMine: Boolean)
}
