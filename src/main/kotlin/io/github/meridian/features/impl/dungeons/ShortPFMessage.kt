package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ChatBlocker
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.simulateGameMessage
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

object ShortPFMessage : SwitchFeature(
    name = "Short Party Finder Message",
    description = "Shortens all party finder queue messages.",
    category = "Dungeons",
    configKey = "short_pf_message",
    subcategory = "Miscellaneous",
) {
    private val joinRegex =
        Regex("""^Party Finder > (\S+) joined the dungeon group! \((Tank|Archer|Mage|Healer|Berserk) Level (\d+)\)$""")

    private val replacements = listOf(
        "Party Finder > Your party has been queued in the dungeon finder!" to "§dPF §f> §aQueued",
        "Party Finder > Your group has been de-listed!" to "§dPF §f> §cDe-listed",
        "Party Finder > Your group has been removed from the party finder!" to "§dPF §f> §cGroup Removed",
        "Party Finder > This group has been de-listed." to "§dPF §f> §cGroup Not Found",
    )

    init {
        ChatBlocker.register({ enabled }, "Queueing your party...")
        ChatBlocker.register({ enabled }, "De-listing your group...")
        ChatBlocker.register({ enabled }, "You are already queued with a party!")
        ChatBlocker.register({ enabled }, "Refreshing...")
        ChatBlocker.register({ enabled }, "Attempting to add you to the party...")
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay || !enabled) return@register true
            val plain = message.string

            joinRegex.find(plain)?.let { m ->
                val (ign, cls, lvl) = m.destructured
                simulateGameMessage("§dPF §f> §b$ign §ajoined §7(§e$cls $lvl§7)")
                return@register false
            }

            replacements.firstOrNull { plain.startsWith(it.first) }
                ?.let { sendClientMessage(it.second); return@register false }

            true
        }
    }
}


