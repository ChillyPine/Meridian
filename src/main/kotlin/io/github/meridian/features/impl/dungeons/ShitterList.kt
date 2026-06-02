package io.github.meridian.features.impl.dungeons

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.meridian.features.ButtonFeature
import io.github.meridian.features.FeatureManager
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.sendCommand
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

object ShitterListButton : ButtonFeature(
    name = "Open Shitter List GUI",
    description = "Manage the list of blacklisted players in a GUI.",
    category = "Dungeons",
    configKey = "shitter_list_button",
    subcategory = "Miscellaneous",
    buttonLabel = "Open GUI",
    onClick = {
        sendCommand("md shitter gui")
    }
)

object ShitterList {
    private const val PAGE_SIZE = 8
    private const val PREFIX = "§6Shitter §5»§r "

    private val players = mutableListOf<String>()

    fun all(): List<String> = players.toList()
    fun size(): Int = players.size
    fun contains(name: String): Boolean =
        players.any { it.equals(name.trim(), ignoreCase = true) }

    fun add(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || contains(trimmed)) return false
        players += trimmed
        players.sortBy { it.lowercase() }
        FeatureManager.save()
        return true
    }

    fun remove(name: String): Boolean {
        val idx = players.indexOfFirst { it.equals(name.trim(), ignoreCase = true) }
        if (idx == -1) return false
        players.removeAt(idx)
        FeatureManager.save()
        return true
    }

    fun reset() {
        players.clear()
        FeatureManager.save()
    }

    fun addCommand(raw: String) {
        val names = splitNames(raw)
        if (names.isEmpty()) {
            modMessage("§cUsage: /md shitter add <player> [player...]", PREFIX)
            return
        }
        for (name in names) {
            if (add(name)) modMessage("§fAdded §b$name §fto the shitter list.", PREFIX)
            else modMessage("§b$name §fis already on the shitter list.", PREFIX)
        }
    }

    fun removeCommand(raw: String) {
        val names = splitNames(raw)
        if (names.isEmpty()) {
            modMessage("§cUsage: /md shitter remove <player> [player...]", PREFIX)
            return
        }
        if (players.isEmpty()) {
            modMessage("§fThe shitter list is empty.", PREFIX)
            return
        }
        for (name in names) {
            if (remove(name)) modMessage("§fRemoved §b$name §ffrom the shitter list.", PREFIX)
            else modMessage("§b$name §fis not on the shitter list.", PREFIX)
        }
    }

    fun listCommand(pageArg: Int) {
        if (players.isEmpty()) {
            modMessage("§fThe shitter list is empty.", PREFIX)
            return
        }
        val totalPages = (players.size + PAGE_SIZE - 1) / PAGE_SIZE
        if (pageArg !in 1..totalPages) {
            modMessage("§cInvalid page. Valid pages: 1-$totalPages.", PREFIX)
            return
        }
        val start = (pageArg - 1) * PAGE_SIZE
        val pageItems = players.subList(start, minOf(start + PAGE_SIZE, players.size))

        sendClientMessage("§r§5§m                                                            §r")

        val prev =
            if (pageArg > 1) link("§e§l<< ", "/md shitter list ${pageArg - 1}", "§eGo to page ${pageArg - 1}")
            else Component.literal("§8§l<< ")
        val next =
            if (pageArg < totalPages) link(" §e§l>>", "/md shitter list ${pageArg + 1}", "§eGo to page ${pageArg + 1}")
            else Component.literal(" §8§l>>")
        val header = Component.literal("            ")
            .append(prev)
            .append(Component.literal("§6Shitter List §7(Page $pageArg/$totalPages)"))
            .append(next)
        sendClientMessage(header)

        for (player in pageItems) {
            val line = Component.literal("§7 - ")
                .append(link("§b$player", "/md shitter remove $player", "§cClick to remove §b$player §cfrom the list"))
            sendClientMessage(line)
        }

        sendClientMessage("§r§5§m                                                            §r")
    }

    fun promptReset() {
        if (players.isEmpty()) {
            modMessage("§fThe shitter list is already empty.", PREFIX)
            return
        }
        val msg = Component.literal("${PREFIX}§cReset the entire shitter list (${players.size} player${plural(players.size)})? ")
            .append(link("§a§l[YES]", "/md shitter resetconfirm", "§aConfirm — wipe the list"))
            .append(Component.literal(" "))
            .append(link("§c§l[NO]", "/md shitter resetcancel", "§cCancel"))
        sendClientMessage(msg)
    }

    fun confirmReset() {
        reset()
        modMessage("§fThe shitter list has been reset.", PREFIX)
    }

    fun cancelReset() {
        modMessage("§fReset cancelled.", PREFIX)
    }

    fun saveTo(json: JsonObject) {
        val arr = JsonArray()
        players.forEach(arr::add)
        json.add("players", arr)
    }

    fun loadFrom(json: JsonObject) {
        players.clear()
        json.getAsJsonArray("players")?.forEach { players += it.asString }
        players.sortBy { it.lowercase() }
    }

    private fun splitNames(raw: String): List<String> =
        raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun plural(n: Int): String = if (n == 1) "" else "s"

    private fun link(label: String, command: String?, hover: String): MutableComponent =
        Component.literal(label).withStyle { style ->
            var s = style.withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
            if (command != null) s = s.withClickEvent(ClickEvent.RunCommand(command))
            s
        }
}
