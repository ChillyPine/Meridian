package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import net.minecraft.network.chat.Component

//- ***Blood Open***
//- ***Blood Full***
//- ***Blood Cleared***
//- ***Send Blood Progress in Party Chat
object BloodNotifs : SwitchFeature(
    name = "Blood Room Notifications",
    description = "Global toggle for blood room notifications.",
    category = "Dungeons",
    configKey = "blood_notifs",
    subcategory = "Clear"
)

object BloodOpen : SwitchFeature(
    name = "Blood Opened",
    description = "Notifies you when the blood door is opened.",
    category = "Dungeons",
    configKey = "blood_open",
    subcategory = "Clear",
    dependsOn = BloodNotifs
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("The BLOOD DOOR has been opened!")) return@onChatMessage
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.literal("§cBlood Opened!"))
            mc.gui.setSubtitle(Component.empty())
            modMessage("§fBlood Opened!")
            if (SendBloodToParty.enabled) {
                sendCommand("pc Blood Opened!")
            }
        }
    }
}

object BloodFull : SwitchFeature(
    name = "Blood Full",
    description = "Notifies you when the blood room is full.",
    category = "Dungeons",
    configKey = "blood_full",
    subcategory = "Clear",
    dependsOn = BloodNotifs
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("[BOSS] The Watcher: That will be enough for now.")) return@onChatMessage
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.literal("§cBlood Full!"))
            mc.gui.setSubtitle(Component.empty())
            modMessage("§fBlood Full!")
            if (SendBloodToParty.enabled) {
                sendCommand("pc Blood Full!")
            }
        }
    }
}

object BloodCleared : SwitchFeature(
    name = "Blood Cleared",
    description = "Notifies you when the blood room has been cleared.",
    category = "Dungeons",
    configKey = "blood_cleared",
    subcategory = "Clear",
    dependsOn = BloodNotifs
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith("[BOSS] The Watcher: You have proven yourself. You may pass.")) return@onChatMessage
            // mc.gui.setTimes(fadeIn, stay, fadeOut)
            mc.gui.setTimes(0, 50, 0)
            mc.gui.setTitle(Component.literal("§cBlood Cleared!"))
            mc.gui.setSubtitle(Component.empty())
            modMessage("§fBlood Cleared!")
            if (SendBloodToParty.enabled) {
                sendCommand("pc Blood Cleared!")
            }
        }
    }
}

object SendBloodToParty : SwitchFeature(
    name = "Send Blood Progress in Party Chat",
    description = "",
    category = "Dungeons",
    configKey = "send_blood_to_party",
    subcategory = "Clear",
    dependsOn = BloodNotifs
)