package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P3State
import net.minecraft.world.entity.decoration.ArmorStand

object TerminalHitboxes : SwitchFeature(
    name = "Terminals True Hitboxes",
    description = "Displays terminals true hitboxes and if it is in range.",
    category = "Dungeons",
    configKey = "terminal_hitboxes",
    subcategory = "P3",
) {
    private const val REACH = 3.0 // unless you a dirty cheater >:(

    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("CLICK HERE")) continue

                val inRange = player.distanceTo(ent) <= REACH

                if (inRange && P3State.inP3) {
                    ESP.drawFilled(ctx, ent, w = 0.5, h = 1.0, wz = 0.5, yOffset = 1.0, InRangeColor.color)
                }
                if(!inRange && P3State.inP3) {
                    ESP.drawFilled(ctx, ent, w = 0.5, h = 1.0, wz = 0.5, yOffset = 1.0, OutRangeColor.color)
                }
            }
        }
    }
}

object InRangeColor : ColorFeature(
    name = "In Range Color",
    description = "",
    category = "Dungeons",
    configKey = "in_range_color",
    subcategory = "P3",
    dependsOn = TerminalHitboxes,
)

object OutRangeColor : ColorFeature(
    name = "Out Of Range Color",
    description = "",
    category = "Dungeons",
    configKey = "out_range_color",
    subcategory = "P3",
    dependsOn = TerminalHitboxes,
)