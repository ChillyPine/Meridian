package io.github.meridian.features.impl.events

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.onChatMessage
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object JerryNotif : SwitchFeature(
    name = "Jerry Spawn Notifier",
    description = "Shows a title when you find a Jerry.",
    category = "Events",
    configKey = "jerry_notif",
    subcategory = "Jerrypocalypse",
) {
    // Color word that appears in chat -> color the title is drawn in.
    private val jerryColors = mapOf(
        "Green" to ChatFormatting.DARK_GREEN,
        "Blue" to ChatFormatting.BLUE,
        "Purple" to ChatFormatting.DARK_PURPLE,
        "Golden" to ChatFormatting.GOLD,
    )

    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            // The real message is " ☺ You located a hidden Golden Jerry!" — note
            // the leading space, so startsWith("☺") never matched. contains is safe.
            if (!text.contains("☺")) return@onChatMessage

            val color = jerryColors.entries
                .firstOrNull { (name, _) -> text.contains("$name Jerry") }
                ?: return@onChatMessage

            // Delay a few ticks in case Hypixel pushes its own title here too.
            TickScheduler.schedule(3) {
                mc.gui.setTimes(0, 35, 0)
                mc.gui.setSubtitle(Component.empty())
                mc.gui.setTitle(
                    Component.literal("Jerry").withStyle(color.value)
                )
            }
        }
    }
}
