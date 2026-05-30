package io.github.meridian.features.impl.general

import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendClientMessage
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// Party list quick actions, extra party list quick actions
//
// When the server prints the party list (via /p list, /p, etc.) this scans the
// Leader / Moderators / Members lines and, for each player, drops a clickable
// row of action buttons into the client chat (PV / Kick / Block / Shitterlist,
// plus Promote / Transfer if the extra-actions child is on).
object PLActions : SwitchFeature (
    name = "Party List Quick Actions",
    description = "Lets you quickly Kick, Ignore, Shitterlist, or PV any party member.",
    category = "General",
    configKey = "pl_actions",
    subcategory = "Party"
) {
    // Leader line carries an optional rank bracket before the name; members and
    // mods are parsed positionally (last token of each "● "-delimited entry).
    private val leaderRegex = Regex("^Party Leader: (?:\\[.*?] )?(\\w+) ●$")

    init {
        onChatMessage { text, _, _ ->
            if (!isActive()) return@onChatMessage

            leaderRegex.find(text)?.let {
                emitActions(it.groupValues[1])
                return@onChatMessage
            }

            when {
                text.startsWith("Party Moderators: ") ->
                    namesFrom(text.removePrefix("Party Moderators: ")).forEach(::emitActions)
                text.startsWith("Party Members: ") ->
                    namesFrom(text.removePrefix("Party Members: ")).forEach(::emitActions)
            }
        }
    }

    // Each entry looks like "[RANK] Name" (rank optional); the username is always
    // the final whitespace-delimited token. Entries are separated by " ●".
    private fun namesFrom(rest: String): List<String> =
        rest.trim().split(" ●")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.split(" ").last() }

    // Build the clickable button row and drop it into chat a tick later, so it
    // renders just beneath the party-list line that triggered it.
    private fun emitActions(player: String) {
        val line = buildButtons(player)
        CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
            .execute { sendClientMessage(line) }
    }

    private fun buildButtons(player: String): MutableComponent {
        val line = Component.literal("§6[MD] §b$player §f» ")

        line.append(button("§a[PV] ", "/pv $player", "§aView §b$player's §aprofile"))
        line.append(Component.literal("§7❘ "))
        line.append(button("§c[Kick] ", "/p kick $player", "§cKick §b$player"))
        line.append(Component.literal("§7❘ "))
        line.append(button("§4[Block] ", "/ignore add $player", "§4Block §b$player"))
        line.append(Component.literal("§7❘ "))

        // Shitterlist isn't implemented in Meridian yet, so this button is inert:
        // it shows and hovers, but the click action is intentionally left off.
        // When the list lands, wire it up with:
        //     button(slLabel, "/shitter add $player", "§dAdd §b$player §dto Shitter List")
        val slLabel = if (PLMoreActions.isActive()) "§d[SL]" else "§d[Shitterlist]"
        line.append(button(slLabel, null, "§dAdd §b$player §dto Shitter List"))

        if (PLMoreActions.isActive()) {
            line.append(Component.literal("§7 ❘ "))
            line.append(button("§b[PM] ", "/p promote $player", "§bPromote §b$player §bto Mod"))
            line.append(Component.literal("§7❘ "))
            line.append(button("§6[PT] ", "/p transfer $player", "§6Transfer party to §b$player"))
        }

        return line
    }

    // A label that runs `command` on click (when non-null) and shows `hover` text
    // on mouse-over.
    private fun button(label: String, command: String?, hover: String): MutableComponent =
        Component.literal(label).withStyle { style ->
            var s = style.withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
            if (command != null) s = s.withClickEvent(ClickEvent.RunCommand(command))
            s
        }
}

object PLMoreActions : SwitchFeature (
    name = "Extra Party List Quick Actions",
    description = "Adds Promote and Transfer options to quick actions.",
    category = "General",
    configKey = "pl_more_actions",
    subcategory = "Party",
    dependsOn = PLActions
)
