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

object CarryManagerButton : ButtonFeature(
    name = "Open Carry Manager GUI",
    description = "Manage ongoing carries in a GUI.",
    category = "Carry Helper",
    configKey = "carry_manager_button",
    subcategory = "General",
    buttonLabel = "Open GUI",
    onClick = {
        sendCommand("md carry gui")
    }
)

object CarryManager {
    private const val PAGE_SIZE = 8
    private const val PREFIX = "§6Meridian §5»§r "

    private const val CONFIRM_WINDOW_MS = 10_000L
    private var armedUntil = 0L

    private val players = mutableListOf<String>()

    // how many are completed
    private val carryCounts = mutableMapOf<String, Int>()
    // how many the total amount is
    private val orderedCounts = mutableMapOf<String, Int>()

    fun all(): List<String> = players.toList()
    fun size(): Int = players.size
    fun contains(name: String): Boolean =
        players.any { it.equals(name.trim(), ignoreCase = true) }

    fun add(name: String, orderedCount: Int? = null): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || contains(trimmed)) return false
        players += trimmed
        players.sortBy { it.lowercase() }
        if (orderedCount != null) orderedCounts[trimmed.lowercase()] = orderedCount
        FeatureManager.save()
        return true
    }

    fun remove(name: String): Boolean {
        val idx = players.indexOfFirst { it.equals(name.trim(), ignoreCase = true) }
        if (idx == -1) return false
        val removed = players.removeAt(idx)
        val key = removed.lowercase()
        orderedCounts.remove(key)
        carryCounts.remove(key)
        FeatureManager.save()
        return true
    }

    // removes player from list and wipes both counters (used when an order is fully completed,
    // so a future re-add via /md carry add starts fresh instead of resuming an old completed count)
    fun completeCarry(name: String): Boolean {
        val idx = players.indexOfFirst { it.equals(name.trim(), ignoreCase = true) }
        if (idx == -1) return false
        val removed = players.removeAt(idx)
        val key = removed.lowercase()
        orderedCounts.remove(key)
        carryCounts.remove(key)
        FeatureManager.save()
        return true
    }

    fun reset() {
        players.clear()
        carryCounts.clear()
        orderedCounts.clear()
        FeatureManager.save()
    }

    fun carriesFor(name: String): Int = carryCounts[name.trim().lowercase()] ?: 0
    fun orderedFor(name: String): Int = orderedCounts[name.trim().lowercase()] ?: 0

    fun setOrdered(name: String, count: Int) {
        val trimmed = name.trim()
        if (!contains(trimmed)) return
        orderedCounts[trimmed.lowercase()] = count
        FeatureManager.save()
    }

    // nudges the ordered count by delta (used by shift+click in the GUI), floored at 0
    fun incrementOrdered(name: String, delta: Int): Int? {
        val trimmed = name.trim()
        if (!contains(trimmed)) return null
        val key = trimmed.lowercase()
        val newVal = ((orderedCounts[key] ?: 0) + delta).coerceAtLeast(0)
        orderedCounts[key] = newVal
        FeatureManager.save()
        return newVal
    }

    // button to manually add a completed carry
    fun addCarry(name: String): Int? {
        val trimmed = name.trim()
        if (!contains(trimmed)) return null
        val key = trimmed.lowercase()
        val count = (carryCounts[key] ?: 0) + 1
        carryCounts[key] = count
        FeatureManager.save()
        return count
    }

    // button to manually remove a completed carry
    fun removeCarry(name: String): Int? {
        val trimmed = name.trim()
        if (!contains(trimmed)) return null
        val key = trimmed.lowercase()
        val count = ((carryCounts[key] ?: 0) - 1).coerceAtLeast(0)
        if (count == 0) carryCounts.remove(key) else carryCounts[key] = count
        FeatureManager.save()
        return count
    }

    // finds which tracked client a boss's display name belongs to, using word-boundary matching
    // so "Notch" doesn't incorrectly match inside "Notch2"
    fun matchClient(bossName: String): String? =
        players.firstOrNull { c ->
            Regex("\\b${Regex.escape(c)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(bossName)
        }

    fun addCommand(raw: String) {
        val entries = parseAddArgs(raw)
        if (entries == null) {
            modMessage("§cCaught Error: Incorrect usage: \n Please run /md carry add <player> <count>", PREFIX)
            return
        }
        for ((name, orderedCount) in entries) {
            if (add(name, orderedCount)) {
                modMessage("§fAdded §b$name §fto the carry list. §7(ordered $orderedCount)", PREFIX)
            } else {
                setOrdered(name, orderedCount)
                modMessage("§b$name §fis already being tracked -> updated ordered count to §6$orderedCount§f.", PREFIX)
            }
        }
    }

    fun removeCommand(raw: String) {
        val names = splitNames(raw)
        if (names.isEmpty()) {
            modMessage("§cCaught Error: Incorrect Usage: \n Please run /md carry remove <player>", PREFIX)
            return
        }
        if (players.isEmpty()) {
            modMessage("§fNo active carries", PREFIX)
            return
        }
        for (name in names) {
            if (remove(name)) modMessage("§fRemoved §b$name §ffrom the carry list.", PREFIX)
            else modMessage("§b$name §fis not being tracked", PREFIX)
        }
    }

    fun addCarryCommand(raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) {
            modMessage("§cCaught Error: Incorrect Usage: \n Please run /md carry addcarry <player>", PREFIX)
            return
        }
        val total = addCarry(name)
        if (total == null) {
            modMessage("§b$name §fis not being tracked — add them first.", PREFIX)
            return
        }
        modMessage("§b$name §fnow has §6$total §fcompleted carr${if (total == 1) "y" else "ies"}.", PREFIX)
    }

    fun removeCarryCommand(raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) {
            modMessage("§cCaught Error: Incorrect Usage: \n Please run /md carry removecarry <player>", PREFIX)
            return
        }
        val total = removeCarry(name)
        if (total == null) {
            modMessage("§b$name §fis not being tracked.", PREFIX)
            return
        }
        modMessage("§b$name §fnow has §6$total §fcompleted carr${if (total == 1) "y" else "ies"}.", PREFIX)
    }


    fun resetCommand() {
        if (players.isEmpty()) {
            armedUntil = 0L
            modMessage("§fThe carry list is already empty.", PREFIX)
            return
        }
        if (System.currentTimeMillis() <= armedUntil) {
            armedUntil = 0L
            reset()
            modMessage("§fThe carry list has been reset.", PREFIX)
            return
        }
        armedUntil = System.currentTimeMillis() + CONFIRM_WINDOW_MS
        val msg = Component.literal("${PREFIX}§cReset the entire carry list (${players.size} player${plural(players.size)})? ")
            .append(link("§a§l[CONFIRM]", "/md carry reset", "§aClick to confirm — wipe the list"))
            .append(Component.literal(" §7or run §f/md carry reset §7again within 10s. Ignore to cancel."))
        sendClientMessage(msg)
    }

    fun saveTo(json: JsonObject) {
        val arr = JsonArray()
        players.forEach(arr::add)
        json.add("players", arr)
        val countsObj = JsonObject()
        carryCounts.forEach { (k, v) -> countsObj.addProperty(k, v) }
        json.add("carryCounts", countsObj)
        val orderedObj = JsonObject()
        orderedCounts.forEach { (k, v) -> orderedObj.addProperty(k, v) }
        json.add("orderedCounts", orderedObj)
    }

    fun loadFrom(json: JsonObject) {
        players.clear()
        carryCounts.clear()
        orderedCounts.clear()
        json.getAsJsonArray("players")?.forEach { players += it.asString }
        players.sortBy { it.lowercase() }
        // "carryCounts"/"orderedCounts" are absent in configs written before these features existed.
        json.getAsJsonObject("carryCounts")?.entrySet()?.forEach { (k, v) -> carryCounts[k] = v.asInt }
        json.getAsJsonObject("orderedCounts")?.entrySet()?.forEach { (k, v) -> orderedCounts[k] = v.asInt }
    }

    private fun splitNames(raw: String): List<String> =
        raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }


    private fun parseAddArgs(raw: String): List<Pair<String, Int>>? {
        val tokens = splitNames(raw)
        if (tokens.isEmpty() || tokens.size % 2 != 0) return null
        val entries = mutableListOf<Pair<String, Int>>()
        var i = 0
        while (i < tokens.size) {
            val name = tokens[i]
            val count = tokens[i + 1].toIntOrNull() ?: return null
            entries += name to count
            i += 2
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