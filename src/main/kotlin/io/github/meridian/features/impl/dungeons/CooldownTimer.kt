package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.onChatMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object CooldownTimerHUD : SwitchFeature(
    name = "Cooldown Timer",
    description = "Displays Instance Cooldown Timer",
    category = "Dungeons",
    configKey = "cooldown_timer",
    subcategory = "Miscellaneous",
) {

    var cooldowntimer: String? = null

    private val element = object : HudElement(
        id = "cooldown_timer",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.5f,
    ) {
        override val shadow = false
        override fun content(): List<String> = cooldowntimer?.let { listOf(it) } ?: emptyList()
        override fun preview(): List<String> = listOf("§aCooldown : 30.0s")
    }

    init {
        HudManager.register(element)

        var endTime: Long = -1L  // System.currentTimeMillis() target

        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (text.contains("Queuing... (Attempt 1/3)")) {
                endTime = System.currentTimeMillis() + 30_000L
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            if (!enabled) return@register
            if (endTime < 0L) return@register

            val remaining = endTime - System.currentTimeMillis()
            if (remaining <= 0L) {
                endTime = -1L
                cooldowntimer = null
            } else {
                val secs = remaining / 1000.0
                val color = when {
                    secs > 20.0 -> "§c"
                    secs > 10.0 -> "§6"
                    else        -> "§a"
                }
                cooldowntimer = "${color}Cooldown : ${"%.2f".format(secs)}s"
            }
        }
    }
}