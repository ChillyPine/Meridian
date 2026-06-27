package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.TickTask
import net.minecraft.network.chat.Component

object LockedChestNotif : SwitchFeature(
    name = "Locked Chest Notification",
    description = "Notifies you when trying to open a locked chest.",
    category = "Dungeons",
    configKey = "locked_chest_notification",
    subcategory = "Clear"
) {
    init {
        onChat { text, _, _ ->
            if (!text.startsWith("That chest is locked!")) return@onChat
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 30, 0)
            mc.gui.setTitle(Component.empty())
            mc.gui.setSubtitle(Component.literal("§cLOCKED CHEST"))
        }
    }
}