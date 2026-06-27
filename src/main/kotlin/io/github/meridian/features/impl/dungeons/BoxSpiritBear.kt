package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.F4State
import net.minecraft.world.entity.decoration.ArmorStand

object BoxSpiritBear : SwitchFeature(
    name = "Box Spirit Bear",
    description = "",
    category = "Dungeons",
    configKey = "box_spirit_bear",
    subcategory = "M4",
)  {
    init {
        onRender { ctx ->
            if (!F4State.inF4Boss) return@onRender
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Spirit Bear")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFE07204.toInt())
            }
        }
    }
}