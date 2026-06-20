package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ChatBlocker

object WatcherYapHider : SwitchFeature(
    name = "Watcher Yap Hider",
    description = "Hides some of the watchers yapping. \nDoes NOT conflict with any other Meridian features.",
    category = "Dungeons",
    configKey = "watcher_yap_hider",
    subcategory = "Clear"
) {
    private const val PREFIX = "[BOSS] The Watcher: "

    // Watcher lines to hide. Compared against the text after the prefix.
    private val messagesToCancel = setOf(
        "That one was weak anyway.",
        "Things feel a little more roomy now, eh?",
        "I've knocked down those pillars to go for a more...open concept.",
        "Plus I needed to give my new friends some space to roam...",
        "This guy looks like a fighter.",
        "I'm impressed.",
        "Hmmm... this one!",
        "Ouch.. just kidding.",
        "Go, fight!",
        "Very nice.",
        "Aw, I liked that one.",
        "Go and live again!",
        "You'll do.",
        "Not bad.",
    )

    init {
        ChatBlocker.register({ enabled }) { msg ->
            msg.startsWith(PREFIX) && msg.removePrefix(PREFIX).trim() in messagesToCancel
        }
    }
}
