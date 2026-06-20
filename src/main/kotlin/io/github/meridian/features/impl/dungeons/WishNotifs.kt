package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.F5State
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.onChatMessage
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object M5WishNotif : SwitchFeature(
    name = "M5 Wish Reminder",
    description = "Reminds you to Wish in M5.",
    category = "Dungeons",
    configKey = "m5_wish_notif",
    subcategory = "M5"
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled || !F5State.inF5Boss) return@onChatMessage
            if (!text.startsWith("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) return@onChatMessage
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 60, 0)
            mc.gui.setTitle(Component.literal("§4WISH"))
            mc.gui.setSubtitle(Component.empty())
            // forUI(sound, pitch, volume): zombie "remedy" cure sound
            Minecraft.getInstance().soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.ZOMBIE_VILLAGER_CURE, 0.5f, 0.9f)
            )
        }
    }
}

object P1WishNotif : SwitchFeature(
    name = "P1 Wish Reminder",
    description = "Reminds you to Wish when Maxor is enraged.",
    category = "Dungeons",
    configKey = "p1_wish_notif",
    subcategory = "P1"
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("⚠ Maxor is enraged! ⚠")) return@onChatMessage
            // Hypixel pushes its own title on this same chat line; delay ours a
            // few ticks so it lands after Hypixel's instead of being overwritten.
            TickScheduler.schedule(3) {
                mc.gui.setTimes(0, 40, 0)
                mc.gui.setTitle(Component.literal("§4WISH"))
                mc.gui.setSubtitle(Component.empty())
            }
        }
    }
}

object P3WishNotif : SwitchFeature(
    name = "P3 Wish Reminder",
    description = "Reminds you to Wish during Goldor.",
    category = "Dungeons",
    configKey = "p3_wish_notif",
    subcategory = "P3"
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("The Core entrance is opening!")) return@onChatMessage
            // Hypixel pushes its own title on this same chat line; delay ours a
            // few ticks so it lands after Hypixel's instead of being overwritten.
            TickScheduler.schedule(3) {
                mc.gui.setTimes(0, 40, 0)
                mc.gui.setTitle(Component.literal("§4WISH"))
                mc.gui.setSubtitle(Component.empty())
            }
        }
    }
}