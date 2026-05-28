package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.TickTask
import io.github.meridian.utils.onChatMessage
import net.minecraft.network.chat.Component

private val ffTitles = listOf(
    30 to "§5§kH§r§64§r§5§kH§r",
    50 to "§5§kH§r§63§r§5§kH§r",
    70 to "§5§kH§r§62§r§5§kH§r",
    90 to "§5§kH§r§61§r§5§kH§r",
    110 to "§4§kH§r§6NOW!§r§4§kH§r",
)

object FFTimer : SwitchFeature(
    name = "Fire Freeze Timer",
    description = "",
    category = "Dungeons",
    configKey = "ff_timer",
    subcategory = "M3"
) {
    private val pending = mutableListOf<TickTask>()

    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) return@onChatMessage
            cancelPending()
            ffTitles.forEach { (delay, title) ->
                pending += TickScheduler.schedule(delay) {
                    mc.gui.setTimes(0, 20, 0)
                    mc.gui.setTitle(Component.literal(title))
                    mc.gui.setSubtitle(Component.empty())
                }
            }
        }
    }

    private fun cancelPending() {
        pending.forEach { it.cancel() }
        pending.clear()
    }
}
