package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos

object P4Platform : SwitchFeature(
    name = "Highlight Platform Blocks",
    description = "Highlights the 3x3 area of blocks to mine out so the platform doesn't drop during P4.",
    category = "Dungeons",
    configKey = "p4_platform",
    subcategory = "P4"
) {
    private const val X0 = 53.0; private const val Y0 = 63.0; private const val Z0 = 113.0
    private const val X1 = 56.0; private const val Y1 = 64.0; private const val Z1 = 116.0

    private const val COLOR = 0xFF00FF00.toInt() // opaque green

    @Volatile private var active = false
    private var lastLevel: ClientLevel? = null

    /** True while at least one of the 9 platform blocks is still present. */
    private fun anyBlockPresent(level: ClientLevel): Boolean {
        for (x in 53..55) for (z in 113..115) {
            if (!level.getBlockState(BlockPos(x, 63, z)).isAir) return true
        }
        return false
    }

    init {
        onChat { text, _, _ ->
            if (!DungeonState.inDungeon) return@onChat
            if (text.startsWith("The Core entrance is opening!")) active = true
            else if (text.startsWith("[BOSS] Necron: Goodbye.")) active = false
        }

        // Always-on world-change reset so `active` clears across worlds even while disabled.
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = Meridian.mc.level
            if (level !== lastLevel) {
                lastLevel = level
                active = false
            }
        })

        onRender { ctx ->
            if (!active) return@onRender
            val level = Meridian.mc.level ?: return@onRender
            if (!anyBlockPresent(level)) return@onRender
            ESP.drawWorldBox(ctx, X0, Y0, Z0, X1, Y1, Z1, COLOR, depth = true)
        }
    }
}
