package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.features.types.TextFeature
import io.github.meridian.features.impl.dungeons.AnnounceShitter.partyJoinRegex
import io.github.meridian.features.impl.dungeons.AnnounceShitter.pfJoinRegex
import io.github.meridian.features.impl.dungeons.AnnounceShitter.shortPfJoinRegex
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// Auto kick shitter
// Announce to party
// Custom Shitter Message
object AutoKickShitter : SwitchFeature(
    name = "Auto Kick Shitters",
    description = "Automatically kicks players on the shitter list from your party when they join.",
    category = "Dungeons",
    configKey = "auto_kick_shitter",
    subcategory = "Miscellaneous",
) {

    private val joinRegex =
        Regex("^Party Finder > (\\w+) joined the dungeon group! \\(.+\\)$")
    private val shortJoinRegex =
        Regex("^PF > (\\S+) joined \\(.+\\)$")

    init {
        onChatMessage { text, _, _ ->
            if (!isActive()) return@onChatMessage
            val player = joinRegex.find(text)?.groupValues?.get(1)
                ?: shortJoinRegex.find(text)?.groupValues?.get(1)
                ?: return@onChatMessage
            if (!ShitterList.contains(player)) return@onChatMessage

            CompletableFuture.delayedExecutor(350, TimeUnit.MILLISECONDS)
                .execute { sendCommand("p kick $player") }
        }
    }
}

object AnnounceShitter : SwitchFeature(
    name = "Announce Shitters to Party",
    description = "Sends a message in party chat when a player from the shitter list joins your party.",
    category = "Dungeons",
    configKey = "announce_shitter",
    subcategory = "Miscellaneous",
) {
    val pfJoinRegex =
        Regex("^Party Finder > (\\w+) joined the dungeon group! \\(.+\\)$")
    val shortPfJoinRegex =
        Regex("^PF > (\\S+) joined \\(.+\\)$")
    val partyJoinRegex =
        Regex("^(?:\\[.+?] )?(\\w+) joined the party\\.$")

    init {
        onChatMessage { text, _, _ ->
            if (!isActive()) return@onChatMessage
            val player = pfJoinRegex.find(text)?.groupValues?.get(1)
                ?: shortPfJoinRegex.find(text)?.groupValues?.get(1)
                ?: partyJoinRegex.find(text)?.groupValues?.get(1)
                ?: return@onChatMessage
            if (!ShitterList.contains(player)) return@onChatMessage

            val message = CustomShitterMessage.value.trim()
            if (message.isEmpty()) return@onChatMessage
            CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS)
                .execute { sendCommand("pc ${message.replace("{player}", player)}") }
        }
    }
}

object CustomShitterMessage : TextFeature(
    name = "Custom Shitter Message",
    description = "Use {player} to insert the player's IGN into the message text.\nYou §emust §rinput a message for this feature to work.",
    category = "Dungeons",
    configKey = "custom_shitter_message",
    subcategory = "Miscellaneous",
    dependsOn = AnnounceShitter
)

object SendShitterReason : SwitchFeature(
    name = "Send Shitter Reason",
    description = "Sends the reason the player is on the shitter list to local chat.\nDoes not send in chat for other players to see!",
    category = "Dungeons",
    configKey = "send_shitter_reason",
    subcategory = "Miscellaneous",
) {
    init {
        onChatMessage { text, _, _ ->
            if (!isActive()) return@onChatMessage
            val player = pfJoinRegex.find(text)?.groupValues?.get(1)
                ?: shortPfJoinRegex.find(text)?.groupValues?.get(1)
                ?: partyJoinRegex.find(text)?.groupValues?.get(1)
                ?: return@onChatMessage
            if (!ShitterList.contains(player)) return@onChatMessage

            val reason = ShitterList.reasonFor(player) ?: "No reason set"
            CompletableFuture.delayedExecutor(150, TimeUnit.MILLISECONDS)
                .execute { modMessage("§r§fShitter §r§b$player§r§f joined (§7$reason§r§f)") }
        }
    }
}
