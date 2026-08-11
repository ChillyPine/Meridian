package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.Island
import io.github.meridian.utils.SkyblockLocation
import net.minecraft.world.entity.animal.armadillo.Armadillo

object BoxPangolins : SwitchFeature(
    name = "Box Pangolins",
    description = "",
    category = "Foraging",
    configKey = "box_pangolins",
    subcategory = "Torrhus Canyon",
) {
    init {
        onRender(SkyblockLocation.on(Island.TORRHUS_CANYON)) { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is Armadillo) continue
                ESP.drawBox(ctx, ent, w = 0.8, h = 0.8, wz = 0.8, argb = 0xFFAD716D.toInt())
            }
        }
    }
}