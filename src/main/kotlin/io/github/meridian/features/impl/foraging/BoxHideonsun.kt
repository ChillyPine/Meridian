package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import net.minecraft.world.entity.monster.Shulker
import io.github.meridian.utils.ESP
import io.github.meridian.utils.Island
import io.github.meridian.utils.SkyblockLocation

object BoxHideonsun : SwitchFeature(
    name = "Box Hideonsun",
    description = "",
    category = "Foraging",
    configKey = "box_hideonsun",
    subcategory = "Torrhus Canyon",
) {
    init {
        onRender(SkyblockLocation.on(Island.TORRHUS_CANYON)) { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is Shulker) continue
                ESP.drawBox(ctx, ent, w = 1.0, h = 1.0, wz = 1.0, argb = 0xFF9EF026.toInt())
            }
        }
    }
}