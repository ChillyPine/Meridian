package io.github.meridian.features.impl.farming

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.Keybinds.boundKeyLabel
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendCommand
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

private val pestRegex = Regex("""(\d+)\s+\S+\s+Pests?\s+ha(?:s|ve)\s+spawned in Plot - (.+?)!""")
private val colorCodes = Regex("§.")
private fun stripColors(text: String) = colorCodes.replace(text, "")

// pray people dont name their plots random shit
private val plotNameRegex = Regex("""[\w-]+""")

// tick timers
private const val TITLE_TICKS = 45
private const val PROMPT_TICKS = 1000
private const val PLOT_WINDOW_TICKS = 1000 // stop letting you warp after time
private const val COMMAND_COOLDOWN_TICKS = 20 // prevent spamming and getting kicked

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
                if (SetSpawnKeybind.promptActive || PestPlotTpKeybind.promptActive) mc.gui.hud.setTitle(Component.empty())
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

            mc.gui.hud.setTimes(
                0,
                if (SetSpawnKeybind.enabled || PestPlotTpKeybind.enabled) PROMPT_TICKS else TITLE_TICKS,
                0
            )
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
    @Volatile private var cooldownTicks = 0
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
            if (cooldownTicks > 0) cooldownTicks--
            if (promptTicksLeft > 0 && --promptTicksLeft == 0) mc.gui.hud.clearTitles()

            // drain every tick so presses made during the cooldown never queue up
            var pressed = false
            while (Keybinds.pestSetSpawn.consumeClick()) pressed = true
            if (!pressed || cooldownTicks > 0) return@onTick

            // remember whether this press is answering a pest spawn before the prompt is cleared
            val pestsJustSpawned = promptTicksLeft > 0

            if (pestsJustSpawned) {
                promptTicksLeft = 0
                mc.gui.hud.clearTitles()
            }
            modMessage("§7Set your spawn")
            sendCommand("setspawn")
            cooldownTicks = COMMAND_COOLDOWN_TICKS

            // must come after clearTitles(), otherwise the clear wipes the new subtitle
            if (pestsJustSpawned) PestPlotTpKeybind.showPrompt()
        }
    }

    override fun onDeactivate() {
        if (promptTicksLeft > 0) mc.gui.hud.clearTitles()
        promptTicksLeft = 0
        cooldownTicks = 0
    }
}

object PestPlotTpKeybind : SwitchFeature(
    name = "Quick Plot Teleport",
    description = "Runs /plot tp to the last pest plot when you press the keybind.\nSet Keybind in Minecraft controls.",
    category = "Farming",
    configKey = "pest_plot_tp_keybind",
    subcategory = "Pests"
) {
    @Volatile private var lastPlot: String? = null
    @Volatile private var plotTicksLeft = 0   // > 0 means pests spawned recently
    @Volatile private var promptTicksLeft = 0
    @Volatile private var cooldownTicks = 0
    val promptActive get() = promptTicksLeft > 0

    /** Shows the tp subtitle. Called after setspawn is pressed. */
    fun showPrompt() {
        val plot = lastPlot
        if (!enabled || plot == null) return
        mc.gui.hud.setTimes(0, PROMPT_TICKS, 0)
        mc.gui.hud.setTitle(Component.empty()) // starts the display timer so the subtitle shows immediately
        mc.gui.hud.setSubtitle(
            Component.literal("§7Press §e[${Keybinds.pestPlotTp.boundKeyLabel()}] §7to teleport to §c$plot")
        )
        promptTicksLeft = PROMPT_TICKS
    }

    init {
        onChat { text, _, _ ->
            val match = pestRegex.find(stripColors(text)) ?: return@onChat
            val plot = match.groupValues[2].trim()
            if (!plotNameRegex.matches(plot)) return@onChat

            lastPlot = plot
            plotTicksLeft = PLOT_WINDOW_TICKS

            // If quick set spawn is off, there's nothing to wait for, so show immediately
            if (!SetSpawnKeybind.enabled) {
                if (!PestSpawnNoti.enabled) mc.gui.hud.setTitle(Component.empty())
                showPrompt()
            }
        }

        onTick {
            if (cooldownTicks > 0) cooldownTicks--
            if (plotTicksLeft > 0 && --plotTicksLeft == 0) lastPlot = null
            if (promptTicksLeft > 0 && --promptTicksLeft == 0) mc.gui.hud.clearTitles()

            // drain every tick so presses made during the cooldown never queue up
            var pressed = false
            while (Keybinds.pestPlotTp.consumeClick()) pressed = true
            if (!pressed || cooldownTicks > 0) return@onTick

            val plot = lastPlot ?: return@onTick
            if (promptTicksLeft > 0) {
                promptTicksLeft = 0
                mc.gui.hud.clearTitles()
            }
            modMessage("§7Warping you to $plot")
            sendCommand("plottp $plot")
            cooldownTicks = COMMAND_COOLDOWN_TICKS
        }
    }

    override fun onDeactivate() {
        if (promptTicksLeft > 0) mc.gui.hud.clearTitles()
        promptTicksLeft = 0
        plotTicksLeft = 0
        cooldownTicks = 0
        lastPlot = null
    }
}