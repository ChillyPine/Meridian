package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import net.minecraft.world.entity.monster.Shulker
import io.github.meridian.utils.ESP

object BoxShulkers : SwitchFeature(
    name = "Box Shulkers",
    description = "",
    category = "Foraging",
    configKey = "box_shulkers",
    subcategory = "General",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is Shulker) continue
                ESP.drawBox(ctx, ent, w = 1.0, h = 1.0, wz = 1.0, argb = ShulkerColor.color)
            }
        }
    }
}

object ShulkerColor : ColorFeature(
    name = "Shulker Color",
    description = "",
    category = "Foraging",
    configKey = "shulker_color",
    subcategory = "General",
    dependsOn = BoxShulkers,
)