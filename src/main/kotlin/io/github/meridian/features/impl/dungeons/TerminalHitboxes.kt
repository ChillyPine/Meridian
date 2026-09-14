package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P3State
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.sqrt

object TerminalHitboxes : SwitchFeature(
    name = "Terminals True Hitboxes",
    description = "Displays terminals true hitboxes and if it is in range.",
    category = "Dungeons",
    configKey = "terminal_hitboxes",
    subcategory = "P3",
) {
    private const val REACH = 3.0 // unless you a dirty cheater >:(

    private const val BOX_W = 0.5
    private const val BOX_H = 1.5
    private const val BOX_WZ = 0.5
    private const val Y_OFFSET = 0.5

    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!P3State.inP3 || !name.contains("Inactive Terminal")) continue

                val box = terminalBox(ent)
                val inRange = distanceToBox(player.eyePosition, box) <= REACH

                val color = if (inRange) InRangeColor.color else OutRangeColor.color
                ESP.drawFilled(ctx, ent, w = BOX_W, h = BOX_H, wz = BOX_WZ, yOffset = Y_OFFSET, color)
            }
        }
    }

    // math and shit to find true angles for player reach
    private fun terminalBox(ent: ArmorStand): AABB {
        val base = ent.position()
        return AABB(
            base.x - BOX_W / 2.0, base.y + Y_OFFSET, base.z - BOX_WZ / 2.0,
            base.x + BOX_W / 2.0, base.y + Y_OFFSET + BOX_H, base.z + BOX_WZ / 2.0
        )
    }

    private fun distanceToBox(origin: Vec3, box: AABB): Double {
        val dx = max(box.minX - origin.x, max(0.0, origin.x - box.maxX))
        val dy = max(box.minY - origin.y, max(0.0, origin.y - box.maxY))
        val dz = max(box.minZ - origin.z, max(0.0, origin.z - box.maxZ))
        return sqrt(dx * dx + dy * dy + dz * dz)
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