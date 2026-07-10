package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendCommand
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

// Bonzo Spirit Phoenix USED
object MaskUsed : SwitchFeature(
    name = "Mask Used",
    description = "Notifies you when your Bonzo Mask, Spirit Mask, or Phoenix Pet pop.",
    category = "Dungeons",
    configKey = "mask_used",
    subcategory = "Miscellaneous"
) {
    init {
        onChat { text, _, _ ->
            when {
                text.startsWith("Your \uE068 Bonzo's Mask saved your life!") -> {
                    mc.gui.setTimes(0, 50, 0)
                    mc.gui.setTitle(Component.empty())
                    mc.gui.setSubtitle(Component.literal("§cBonzo Mask Used!"))
                    if (PlayProcSound.enabled) {
                        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
                    }
                    if (SendMaskInPartyChat.enabled) {
                        sendCommand("pc Bonzo Mask Used!")
                    }
                }

                text.startsWith("Second Wind Activated! Your Spirit Mask saved your life!") -> {
                    mc.gui.setTimes(0, 50, 0)
                    mc.gui.setTitle(Component.empty())
                    mc.gui.setSubtitle(Component.literal("§cSpirit Mask Used!"))
                    if (PlayProcSound.enabled) {
                        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
                    }
                    if (SendMaskInPartyChat.enabled) {
                        sendCommand("pc Spirit Mask Used!")
                    }
                }

                text.startsWith("Your Phoenix Pet saved you from certain death!") -> {
                    mc.gui.setTimes(0, 50, 0)
                    mc.gui.setTitle(Component.empty())
                    mc.gui.setSubtitle(Component.literal("§cPhoenix Pet Used!"))
                    if (PlayProcSound.enabled) {
                        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.0f))
                    }
                    if (SendMaskInPartyChat.enabled) {
                        sendCommand("pc Phoenix Pet Used!")
                    }
                }
            }
        }
    }
}

object SendMaskInPartyChat : SwitchFeature(
    name = "Send Mask Pop in Party Chat",
    description = "",
    category = "Dungeons",
    configKey = "send_mask_to_party",
    subcategory = "Miscellaneous",
    dependsOn = MaskUsed
)

object PlayProcSound : SwitchFeature(
    name = "Play Sound on Mask Pop",
    description = "",
    category = "Dungeons",
    configKey = "mask_proc_sound",
    subcategory = "Miscellaneous",
    dependsOn = MaskUsed
)