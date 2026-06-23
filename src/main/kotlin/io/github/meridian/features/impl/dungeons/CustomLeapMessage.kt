package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.features.types.TextFeature
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object CustomLeapMessage : SwitchFeature(
    name = "Custom Leap Message",
    description = "",
    category = "Dungeons",
    configKey = "custom_leap_message",
    subcategory = "Miscellaneous",
) {
    private val LeapMessageRegex =
        Regex("You have teleported to (\\w+)!") //You have teleported to {IGN}!

    init {
        onChatMessage { text, _, _ ->
            if (!isActive()) return@onChatMessage

            val player = LeapMessageRegex.find(text)?.groupValues?.get(1) ?: return@onChatMessage

            val message = CustomLeapMessageTXT.value.trim()
            if (message.isEmpty()) return@onChatMessage
            CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS)
                .execute { sendCommand("pc ${message.replace("{player}", player)}") }
        }
    }
}

object CustomLeapMessageTXT : TextFeature(
    name = "Custom Leap Message Text",
    description = "Use {player} to insert the player's IGN into the message text.\nYou §emust §rinput a message for this feature to work.",
    category = "Dungeons",
    configKey = "custom_leap_message_text",
    subcategory = "Miscellaneous",
    dependsOn = CustomLeapMessage
)
