package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.simulateGameMessage

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
        blockChat("Queueing your party...")
        blockChat("De-listing your group...")
        blockChat("You are already queued with a party!")
        blockChat("Refreshing...")
        blockChat("Attempting to add you to the party...")
        blockChatRaw { message ->
            val plain = message.string

            joinRegex.find(plain)?.let { m ->
                val (ign, cls, lvl) = m.destructured
                simulateGameMessage("§dPF §f> §b$ign §ajoined §7(§e$cls $lvl§7)")
                return@blockChatRaw true
            }

            replacements.firstOrNull { plain.startsWith(it.first) }
                ?.let { sendClientMessage(it.second); return@blockChatRaw true }

            false
        }
    }
}


