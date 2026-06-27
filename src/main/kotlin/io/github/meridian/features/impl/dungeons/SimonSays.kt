package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendCommand
import io.github.meridian.utils.TickScheduler
import net.minecraft.client.Minecraft

// Big thanks to Soon2BeATaco_ for some of the logic and regex here! :D <3

private val deviceRegex = Regex("""^([\w_]+) completed a device! \(\d/7\)""")

private class SimonSaysTracker {
    private var ssStartTime: Long? = null
    private var announced = false

    fun detect(text: String): Double? {
        if (text.startsWith("[BOSS] Goldor: Who dares trespass into my domain?")) {
            ssStartTime = System.currentTimeMillis()
            announced = false
            return null
        }

        if (announced) return null
        val start = ssStartTime ?: return null

        val playerName = deviceRegex.find(text)?.groupValues?.getOrNull(1) ?: return null

        val mc = Minecraft.getInstance()
        val entity = mc.level?.entitiesForRendering()
            ?.find { it.name.string == playerName } ?: return null

        if (entity.distanceToSqr(109.0, 120.0, 94.0) > 5) return null

        announced = true
        ssStartTime = null
        return (System.currentTimeMillis() - start) / 1000.0
    }
}

object SimonSaysTime : SwitchFeature(
    name = "Simon Says Time",
    description = "Sends a local chat message with the Simon Says completion time.",
    category = "Dungeons",
    configKey = "simon_says_time",
    subcategory = "P3",
) {
    private val tracker = SimonSaysTracker()

    private fun timeColor(seconds: Double): String = when {
        seconds < 12.0  -> "§a"
        seconds <= 15.0 -> "§e"
        else            -> "§c"
    }

    init {
        onChat(DungeonState.state) { text, _, _ ->
            val elapsed = tracker.detect(text) ?: return@onChat
            TickScheduler.schedule(1) {
                modMessage("§fSimon Says Took: ${timeColor(elapsed)}${String.format("%.2f", elapsed)}s")
            }
        }
    }
}

object SimonSaysPC : SwitchFeature(
    name = "Simon Says Party Notification",
    description = "Sends a message in party chat with the Simon Says completion time.",
    category = "Dungeons",
    configKey = "simon_says_pc",
    subcategory = "P3",
) {
    private val tracker = SimonSaysTracker()

    init {
        onChat(DungeonState.state) { text, _, _ ->
            val elapsed = tracker.detect(text) ?: return@onChat
            sendCommand("pc Simon Says Took: ${String.format("%.2f", elapsed)}s")
        }
    }
}