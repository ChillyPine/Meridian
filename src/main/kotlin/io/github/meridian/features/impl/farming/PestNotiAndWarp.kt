package io.github.meridian.features.impl.farming

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.Keybinds.boundKeyLabel
import io.github.meridian.utils.sendCommand
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

private val pestRegex = Regex("""(\d+)\s+\S+\s+Pests?\s+ha(?:s|ve)\s+spawned in Plot - (.+?)!""")
private val colorCodes = Regex("§.")
private fun stripColors(text: String) = colorCodes.replace(text, "")

// tick timers
private const val TITLE_TICKS = 45
private const val PROMPT_TICKS = 1000

object PestSpawnNoti : SwitchFeature(
    name = "Pest Spawn Notification",
    description = "On screen and sound",
    category = "Farming",
    configKey = "pest_spawn_notification",
    subcategory = "Pests"
) {
    private const val BEEP_COUNT = 3
    private const val BEEP_INTERVAL_TICKS = 4
    private val beepPitches = listOf(1.2f, 2.0f, 3.0f) // stacked for volume

    @Volatile private var beepsLeft = 0
    @Volatile private var ticksUntilNext = 0
    @Volatile private var titleTicksLeft = 0

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (titleTicksLeft > 0 && --titleTicksLeft == 0) {
                if (SetSpawnKeybind.promptActive) mc.gui.hud.setTitle(Component.empty())
                else mc.gui.hud.clearTitles()
            }

            if (beepsLeft > 0 && --ticksUntilNext <= 0) {
                beepPitches.forEach {
                    mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_COW_BELL, it))
                }
                beepsLeft--
                ticksUntilNext = BEEP_INTERVAL_TICKS
            }
        }

        onChat { text, _, _ ->
            val match = pestRegex.find(stripColors(text)) ?: return@onChat
            val (count, plot) = match.destructured

            mc.gui.hud.setTimes(0, if (SetSpawnKeybind.enabled) PROMPT_TICKS else TITLE_TICKS, 0)
            mc.gui.hud.setTitle(Component.literal("§c$count §7Pests in §c$plot"))
            titleTicksLeft = TITLE_TICKS

            beepsLeft = BEEP_COUNT
            ticksUntilNext = 0
        }
    }

    override fun onDeactivate() {
        beepsLeft = 0
        titleTicksLeft = 0
    }
}

object SetSpawnKeybind : SwitchFeature(
    name = "Quick Set Spawn",
    description = "Runs /setspawn when you press the keybind.\nSet Keybind in Minecraft controls.",
    category = "Farming",
    configKey = "set_spawn_keybind",
    subcategory = "Pests"
) {
    @Volatile private var promptTicksLeft = 0
    val promptActive get() = promptTicksLeft > 0

    init {
        onChat { text, _, _ ->
            if (!pestRegex.containsMatchIn(stripColors(text))) return@onChat

            mc.gui.hud.setTimes(0, PROMPT_TICKS, 0)
            if (!PestSpawnNoti.enabled) mc.gui.hud.setTitle(Component.empty())
            mc.gui.hud.setSubtitle(
                Component.literal("§7Press §e[${Keybinds.pestSetSpawn.boundKeyLabel()}] §7to set spawn")
            )
            promptTicksLeft = PROMPT_TICKS
        }

        onTick {
            if (promptTicksLeft > 0 && --promptTicksLeft == 0) mc.gui.hud.clearTitles()

            var pressed = false
            while (Keybinds.pestSetSpawn.consumeClick()) pressed = true
            if (!pressed) return@onTick

            if (promptTicksLeft > 0) {
                promptTicksLeft = 0
                mc.gui.hud.clearTitles()
            }
            sendCommand("setspawn")
        }
    }

    override fun onDeactivate() {
        if (promptTicksLeft > 0) mc.gui.hud.clearTitles()
        promptTicksLeft = 0
    }
}