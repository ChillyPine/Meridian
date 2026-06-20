package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.TickTask
import io.github.meridian.utils.onChatMessage
import net.minecraft.network.chat.Component

object LockedChestNotif : SwitchFeature(
    name = "Locked Chest Notification",
    description = "Notifies you when trying to open a locked chest.",
    category = "Dungeons",
    configKey = "locked_chest_notification",
    subcategory = "Clear"
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("That chest is locked!")) return@onChatMessage
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 30, 0)
            mc.gui.setTitle(Component.empty())
            mc.gui.setSubtitle(Component.literal("§cLOCKED CHEST"))
        }
    }
}