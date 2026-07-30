package io.github.meridian.features.impl.dungeons

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.meridian.features.types.ButtonFeature
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
        sendCommand("md shitter gui", delayMs = 0)
    }
)

object ShitterList {
    private const val PAGE_SIZE = 8
    private const val PREFIX = "§6Meridian §5»§r "

    private const val CONFIRM_WINDOW_MS = 10_000L
    private var armedUntil = 0L

    private val players = mutableListOf<String>()
    // Reasons keyed by lowercased IGN, kept separate from [players] so the
    // ordering/casing of the visible name list stays untouched. Never surfaced
    // in `/md shitter list` — only the GUI reads these (on hover / edit).
    private val reasons = mutableMapOf<String, String>()

    fun all(): List<String> = players.toList()
    fun size(): Int = players.size
    fun contains(name: String): Boolean =
        players.any { it.equals(name.trim(), ignoreCase = true) }

    /** The reason for [name], or null if none is set (blanks treated as none). */
    fun reasonFor(name: String): String? =
        reasons[name.trim().lowercase()]?.takeIf { it.isNotBlank() }

    /** Sets/updates/clears the reason for an already-listed player. */
    fun setReason(name: String, reason: String) {
        val trimmed = name.trim()
        if (!contains(trimmed)) return
        val key = trimmed.lowercase()
        val r = reason.trim()
        if (r.isEmpty()) reasons.remove(key) else reasons[key] = r
        FeatureManager.save()
    }

    fun add(name: String, reason: String? = null): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || contains(trimmed)) return false
        players += trimmed
        players.sortBy { it.lowercase() }
        if (!reason.isNullOrBlank()) reasons[trimmed.lowercase()] = reason.trim()
        FeatureManager.save()
        return true
    }

    fun remove(name: String): Boolean {
        val idx = players.indexOfFirst { it.equals(name.trim(), ignoreCase = true) }
        if (idx == -1) return false
        val removed = players.removeAt(idx)
        reasons.remove(removed.lowercase())
        FeatureManager.save()
        return true
    }

    fun reset() {
        players.clear()
        reasons.clear()
        FeatureManager.save()
    }

    fun addCommand(raw: String) {
        val entries = parseAddArgs(raw)
        if (entries.isEmpty()) {
            modMessage("§cUsage: /md shitter add <player> [\"reason\"] [player...]", PREFIX)
            return
        }
        for ((name, reason) in entries) {
            if (add(name, reason)) {
                if (reason != null) modMessage("§fAdded §b$name §fto the shitter list. §7($reason)", PREFIX)
                else modMessage("§fAdded §b$name §fto the shitter list.", PREFIX)
            } else if (reason != null) {
                setReason(name, reason)
                modMessage("§b$name §fis already listed — updated reason. §7($reason)", PREFIX)
            } else {
                modMessage("§b$name §fis already on the shitter list.", PREFIX)
            }
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

    fun resetCommand() {
        if (players.isEmpty()) {
            armedUntil = 0L
            modMessage("§fThe shitter list is already empty.", PREFIX)
            return
        }
        if (System.currentTimeMillis() <= armedUntil) {
            armedUntil = 0L
            reset()
            modMessage("§fThe shitter list has been reset.", PREFIX)
            return
        }
        armedUntil = System.currentTimeMillis() + CONFIRM_WINDOW_MS
        val msg = Component.literal("${PREFIX}§cReset the entire shitter list (${players.size} player${plural(players.size)})? ")
            .append(link("§a§l[CONFIRM]", "/md shitter reset", "§aClick to confirm — wipe the list"))
            .append(Component.literal(" §7or run §f/md shitter reset §7again within 10s. Ignore to cancel."))
        sendClientMessage(msg)
    }

    fun saveTo(json: JsonObject) {
        val arr = JsonArray()
        players.forEach(arr::add)
        json.add("players", arr)
        val reasonObj = JsonObject()
        reasons.forEach { (k, v) -> reasonObj.addProperty(k, v) }
        json.add("reasons", reasonObj)
    }

    fun loadFrom(json: JsonObject) {
        players.clear()
        reasons.clear()
        json.getAsJsonArray("players")?.forEach { players += it.asString }
        players.sortBy { it.lowercase() }
        // "reasons" is absent in configs written before this feature existed.
        json.getAsJsonObject("reasons")?.entrySet()?.forEach { (k, v) -> reasons[k] = v.asString }
    }

    private fun splitNames(raw: String): List<String> =
        raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    /**
     * Parses an `add` argument string into (name, reason?) entries. Bare tokens
     * are names; a quoted segment (straight or curly quotes) is a reason that
     * attaches to the most recently seen name, e.g.
     *   `Notch "He cheated" Dinnerbone`  -> [(Notch, "He cheated"), (Dinnerbone, null)]
     */
    private fun parseAddArgs(raw: String): List<Pair<String, String?>> {
        val entries = mutableListOf<Pair<String, String?>>()
        val s = raw.trim()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c == '"' || c == '“' || c == '”' -> {
                    val sb = StringBuilder()
                    i++
                    while (i < s.length && s[i] != '"' && s[i] != '“' && s[i] != '”') {
                        sb.append(s[i]); i++
                    }
                    if (i < s.length) i++ // consume closing quote
                    val reason = sb.toString().trim()
                    // Attach to the previous name; a leading/orphan quote is ignored.
                    if (reason.isNotEmpty() && entries.isNotEmpty()) {
                        entries[entries.lastIndex] = entries.last().first to reason
                    }
                }
                else -> {
                    val sb = StringBuilder()
                    while (i < s.length && !s[i].isWhitespace() &&
                           s[i] != '"' && s[i] != '“' && s[i] != '”') {
                        sb.append(s[i]); i++
                    }
                    if (sb.isNotEmpty()) entries += sb.toString() to null
                }
            }
        }
        return entries
    }

    private fun plural(n: Int): String = if (n == 1) "" else "s"

    private fun link(label: String, command: String?, hover: String): MutableComponent =
        Component.literal(label).withStyle { style ->
            var s = style.withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
            if (command != null) s = s.withClickEvent(ClickEvent.RunCommand(command))
            s
        }
}
