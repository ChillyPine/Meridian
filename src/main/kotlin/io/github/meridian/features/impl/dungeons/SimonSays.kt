package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import net.minecraft.client.Minecraft

// Big thanks to Soon2BeATaco_ for some of the logic and regex here! :D <3
// TODO: Add party notifcation for SS time
object SimonSaysTime : SwitchFeature(
    name = "Simon Says Time",
    description = "Sends a local chat message with the Simon Says completion time.",
    category = "Dungeons",
    configKey = "simon_says_time",
    subcategory = "P3",
) {
    private var ssStartTime: Long? = null
    private var announced = false
    private val deviceRegex = Regex("""^([\w_]+) completed a device! \(\d/7\)""")

    private fun timeColor(seconds: Double): String = when {
        seconds < 12.0  -> "§a"
        seconds <= 15.0 -> "§e"
        else            -> "§c"
    }

    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage

            if (text.startsWith("[BOSS] Goldor: Who dares trespass into my domain?")) {
                ssStartTime = System.currentTimeMillis()
                announced = false
                return@onChatMessage
            }

            if (announced) return@onChatMessage
            val start = ssStartTime ?: return@onChatMessage

            val playerName = deviceRegex.find(text)?.groupValues?.getOrNull(1) ?: return@onChatMessage

            val mc = Minecraft.getInstance()
            val entity = mc.level?.entitiesForRendering()
                ?.find { it.name.string == playerName } ?: return@onChatMessage

            if (entity.distanceToSqr(109.0, 120.0, 94.0) > 5) return@onChatMessage

            val elapsed = (System.currentTimeMillis() - start) / 1000.0
            modMessage("§fSimon Says Took: ${timeColor(elapsed)}${String.format("%.2f", elapsed)}s")
            announced = true
            ssStartTime = null
        }
    }
}