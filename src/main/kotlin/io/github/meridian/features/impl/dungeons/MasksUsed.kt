package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

// Bonzo Spirit Phoenix USED
object MaskUsed : SwitchFeature(
    name = "Mask Used",
    description = "Gives a visual cue of a Bonzo , Spirit, or Phoenix Proc",
    category = "Dungeons",
    configKey = "mask_used_noti",
    subcategory = "Miscellaneous"
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("Your ⚚ Bonzo's Mask saved your life!")) return@onChatMessage
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.empty())
            mc.gui.setSubtitle(Component.literal("§cBonzo Mask Used!"))
            modMessage("§cBonzo Mask Used!")
            if (PlayProcSound.enabled) {
                mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
            }
            if (SendMaskInPartyChat.enabled) {
                sendCommand("pc Bonzo Mask Used!")
            }
        }
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("Second Wind Activated! Your Spirit Mask saved your life!")) return@onChatMessage
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.empty())
            mc.gui.setSubtitle(Component.literal("§cSpirit Mask Used!"))
            modMessage("§cSpirit Mask Used!")
            if (PlayProcSound.enabled) {
                mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
            }
            if (SendMaskInPartyChat.enabled) {
                sendCommand("pc Spirit Mask Used!")
            }
        }
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("Your Phoenix Pet saved you from certain death!")) return@onChatMessage
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.empty())
            mc.gui.setSubtitle(Component.literal("§cPhoenix Pet Used!"))
            modMessage("§cPhoenix Pet Used!")
            if (PlayProcSound.enabled) {
                mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
            }
            if (SendMaskInPartyChat.enabled) {
                sendCommand("pc Phoenix Pet Used!")
            }
        }
    }
}

object SendMaskInPartyChat : SwitchFeature(
    name = "Sends masks being used in party chat",
    description = "",
    category = "Dungeons",
    configKey = "send_mask_to_party",
    subcategory = "Miscellaneous",
    dependsOn = MaskUsed
)

object PlayProcSound : SwitchFeature(
    name = "Plays a sound on Proc",
    description = "",
    category = "Dungeons",
    configKey = "play_proc_sound",
    subcategory = "Miscellaneous",
    dependsOn = MaskUsed
)