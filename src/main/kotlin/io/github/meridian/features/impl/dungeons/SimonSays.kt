package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.world.entity.decoration.ArmorStand

object SimonSaysTime : SwitchFeature(
    name = "Simon Says Time",
    description = "Tells you how long it took to complete simon says",
    category = "Dungeons",
    configKey = "simon_says_time",
    subcategory = "P3",
) {
    private var ssStartTime: Long? = null
    private var announced = false

    private fun inSSBounds(x: Double, y: Double, z: Double): Boolean {
        return y > 110 && x >= 100 && x <= 110 && z >= 90 && z <= 100
    }

    private fun timeColor(seconds: Double): String = when {
        seconds < 12.0  -> "§a"
        seconds <= 15.0 -> "§e"
        else            -> "§c"
    }

    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("[BOSS] Goldor: Who dares trespass into my domain?")) return@onChatMessage
            ssStartTime = System.currentTimeMillis()
            announced = false
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (!enabled) return@EndTick
            val startTime = ssStartTime ?: return@EndTick
            if (announced) return@EndTick
            val level = client.level ?: return@EndTick

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                val pos = ent.position()
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                if (name.contains("Active") && inSSBounds(pos.x, pos.y, pos.z)) {
                    modMessage("§fSimon Says Took: ${timeColor(elapsed)}${String.format("%.2f", elapsed)}s")
                    announced = true
                    ssStartTime = null
                    break
                }
            }
        })
    }
}
//TODO: Send that shit in party chat
/*
object SendSSTime : SwitchFeature(
    name = "Send Simon Says Time",
    description = "Sends how long it took to complete simon says in party chat",
    category = "Dungeons",
    configKey = "send_sstime_in_party",
    subcategory = "P3",
    dependsOn = SimonSaysTime
)
*/
