package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.sendClientMessage
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

object ShortPFMessage : SwitchFeature(
    name = "Short Party Finder Message",
    description = "Shortens all party finder queue messages.",
    category = "Dungeons",
    configKey = "short_pf_message",
    subcategory = "Miscellaneous",
) {
    private val replacements = listOf(
        "Party Finder > Your party has been queued in the dungeon finder!"  to "§dPF §f> §aQueued",
        "Party Finder > Your group has been de-listed!"                     to "§dPF §f> §cDe-listed",
        "Party Finder > Your group has been removed from the party finder!" to "§dPF §f> §cGroup Removed",
        "Party Finder > This group has been de-listed."                     to "§dPF §f> §cGroup Not Found",
    )
    init {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay || !enabled) return@register true
            val plain = message.string
            val match = replacements.firstOrNull { plain.contains(it.first) }
                ?: return@register true
            sendClientMessage(match.second)
            false
        }
    }
}