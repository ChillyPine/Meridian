package io.github.meridian.features.impl.farming

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

object PestSpawnNoti : SwitchFeature(
    name = "Pest Spawn Notification",
    description = "On screen and sound",
    category = "Farming",
    configKey = "pest_spawn_notification",
    subcategory = "Pests"
) {

    private val pestRegex = Regex("""(\d+)\s+\S+\s+Pests?\s+ha(?:s|ve)\s+spawned in Plot - (.+?)!""")
    private val colorCodes = Regex("§.")

    private const val BEEP_COUNT = 3
    private const val BEEP_INTERVAL_TICKS = 4

    @Volatile private var beepsLeft = 0
    @Volatile private var ticksUntilNext = 0

    init {

        ClientTickEvents.END_CLIENT_TICK.register {
            if (beepsLeft > 0 && --ticksUntilNext <= 0) {
                mc.soundManager.play(
                    SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0f)
                )
                beepsLeft--
                ticksUntilNext = BEEP_INTERVAL_TICKS
            }
        }

        onChat { text, _, _ ->
            val clean = colorCodes.replace(text, "")
            val match = pestRegex.find(clean) ?: return@onChat

            val count = match.groupValues[1].toInt()
            val plot = match.groupValues[2]

            mc.gui.hud.setTimes(0, 45, 0)
            mc.gui.hud.setTitle(Component.empty())
            mc.gui.hud.setSubtitle(Component.literal("§c$count Pests in Plot $plot"))

            beepsLeft = BEEP_COUNT
            ticksUntilNext = 0
        }
    }
}