package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.F4State
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.decoration.ArmorStand

object BoxSpiritBear : SwitchFeature(
    name = "Box Spirit Bear",
    description = "",
    category = "Dungeons",
    configKey = "box_spirit_bear",
    subcategory = "M4",
)  {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled || !F4State.inF4Boss) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Spirit Bear")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFE07204.toInt())
            }
        }
    }
}