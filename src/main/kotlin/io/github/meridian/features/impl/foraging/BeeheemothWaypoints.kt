package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.Island
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.Keybinds.boundKeyLabel
import io.github.meridian.utils.SkyblockLocation
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.TickTask
import io.github.meridian.utils.sendCommand
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

/**
 * The three spots the Beeheemoth can spawn at in Torrhus Canyon. [chatName] is
 * the wording Hypixel uses in "A Beeheemoth has spawned at <chatName>!";
 * [warpLabel] and [warpCommand] are null for Mega Tree, which has no warp.
 */
private enum class BeeSpawn(
    val chatName: String,
    val pos: BlockPos,
    val warpLabel: String? = null,
    val warpCommand: String? = null,
) {
    SAFARI("Critter Safari Entrance", BlockPos(-711, 108, 188), "Safari Entrance", "warp safari"),
    SPRINGS("Torrhus Springs", BlockPos(-640, 163, 164), "Springs", "warp springs"),
    MEGA_TREE("Mega Tree", BlockPos(-524, 112, 289)),
}

private const val SPAWN_PREFIX = "A Beeheemoth has spawned at"

private fun parseSpawn(text: String): BeeSpawn? {
    if (!text.contains(SPAWN_PREFIX)) return null
    return BeeSpawn.entries.firstOrNull { text.contains(it.chatName) }
}

private fun isDeathMessage(text: String): Boolean = text.contains("BEEHEEMOTH DOWN")

object BeeheemothWaypoints : SwitchFeature(
    name = "Beeheemoth Spawn Waypoints",
    description = "Marks the Beeheemoth's spawn spot with a beacon beam until it dies.",
    category = "Foraging",
    configKey = "beeheemoth_waypoints",
    subcategory = "Torrhus Canyon",
) {
    private const val COLOR = 0xFFFFC800.toInt() // opaque honey gold

    @Volatile private var spawn: BeeSpawn? = null

    init {
        onChat { text, _, _ ->
            if (isDeathMessage(text)) spawn = null
            else parseSpawn(text)?.let { spawn = it }
        }

        onRender(SkyblockLocation.on(Island.TORRHUS_CANYON)) { ctx ->
            val s = spawn ?: return@onRender
            val p = s.pos
            ESP.drawBeaconBeam(ctx, p.x, p.y, p.z, COLOR)

            val player = mc.player ?: return@onRender
            val cx = p.x + 0.5
            val cz = p.z + 0.5
            val dist = player.position().distanceTo(Vec3(cx, p.y + 0.5, cz))
            ESP.drawWorldLabel(ctx, "Beeheemoth (${dist.toInt()}m)", cx, p.y + 2.5, cz, COLOR)
        }

        // Leaving the island ends the fight for us either way, and the DOWN
        // message won't arrive once we're gone.
        SkyblockLocation.islandState.listen { if (it != Island.TORRHUS_CANYON) spawn = null }
    }

    override fun onDeactivate() {
        spawn = null
    }
}

object BeeheemothQuickWarp : SwitchFeature(
    name = "Beeheemoth Quick Warp",
    description = "Shows a keybind prompt to warp to the Beeheemoth's spawn for 10s after it spawns. \nSet Keybind in Minecraft controls.",
    category = "Foraging",
    configKey = "beeheemoth_quick_warp",
    subcategory = "Torrhus Canyon",
    dependsOn = BeeheemothWaypoints,
) {
    private const val TITLE_TICKS = 30   // 1.5s on screen
    private const val WINDOW_TICKS = 200 // 10s to press the key

    @Volatile private var pending: BeeSpawn? = null
    private var windowTask: TickTask? = null

    init {
        onChat { text, _, _ ->
            if (isDeathMessage(text)) {
                closeWindow()
                return@onChat
            }
            val spawn = parseSpawn(text) ?: return@onChat
            if (spawn.warpCommand == null) return@onChat
            openWindow(spawn)
        }

        onTick {
            // Drain every tick so a press made outside the window never queues up.
            var pressed = false
            while (Keybinds.beeheemothWarp.consumeClick()) pressed = true
            val spawn = pending ?: return@onTick
            if (!pressed) return@onTick
            closeWindow()
            spawn.warpCommand?.let { sendCommand(it) }
        }
    }

    private fun openWindow(spawn: BeeSpawn) {
        windowTask?.cancel()
        pending = spawn
        windowTask = TickScheduler.schedule(WINDOW_TICKS) { closeWindow() }

        mc.gui.setTimes(0, TITLE_TICKS, 0)
        mc.gui.setSubtitle(Component.empty())
        mc.gui.setTitle(
            Component.literal("[${Keybinds.beeheemothWarp.boundKeyLabel()}] warp ${spawn.warpLabel}")
        )
    }

    private fun closeWindow() {
        pending = null
        windowTask?.cancel()
        windowTask = null
    }

    override fun onDeactivate() {
        closeWindow()
    }
}
