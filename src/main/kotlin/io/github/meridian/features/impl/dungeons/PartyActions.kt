package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.sendClientMessage
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

// Actions when someone joins a dungeon party via Party Finder.
object PartyActions : SwitchFeature(
    name = "Party Quick Actions",
    description = "Drops PV / Kick / Block / Shitterlist buttons in chat when a player joins via Party Finder.",
    category = "Dungeons",
    configKey = "party_quick_actions",
    subcategory = "Miscellaneous"
) {
    private val joinRegex =
        Regex("^Party Finder > (\\w+) joined the dungeon group! \\(.+\\)$")
    private val shortJoinRegex =
        Regex("^PF > (\\S+) joined \\(.+\\)$")

    init {
        onChat { text, _, _ ->
            val player = joinRegex.find(text)?.groupValues?.get(1)
                ?: shortJoinRegex.find(text)?.groupValues?.get(1)
                ?: return@onChat

            sendClientMessage(buildButtons(player))
        }
    }

    private fun buildButtons(player: String): MutableComponent {
        val line = Component.literal("§6[MD] §b$player §f» ")

        line.append(button("§a[PV] ", "/pv $player", "§aView §b$player's §aprofile"))
        line.append(Component.literal("§7❘ "))
        line.append(button("§c[Kick] ", "/p kick $player", "§cKick §b$player"))
        line.append(Component.literal("§7❘ "))
        line.append(button("§4[Block] ", "/ignore add $player", "§4Block §b$player"))
        line.append(Component.literal("§7❘ "))
        line.append(button("§d[Shitterlist] ", "/md shitter add $player", "§dAdd §b$player §dto Shitter List"))
        line.append(Component.literal("§7❘"))

        return line
    }

    private fun button(label: String, command: String, hover: String): MutableComponent =
        Component.literal(label).withStyle { style ->
            style.withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
                .withClickEvent(ClickEvent.RunCommand(command))
        }
}
