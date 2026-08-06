package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.decoration.ArmorStand

object BoxShinyCritters : SwitchFeature(
    name = "Box Shiny Critters",
    description = "",
    category = "Foraging",
    configKey = "box_shiny_critters",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                if (!name.contains("Sparkling")) continue
                ESP.drawBox(ctx, ent, w = 1.3, h = 1.0, wz = 1.0, yOffset = -1.0, argb = Shinycrittercolor.color)
                ESP.drawTracer(ctx, p.x, p.y, p.z, Shinycrittercolor.color)
            }
        }
    }
}
object Shinycrittercolor : ColorFeature(
    name = "Shinny Critter Color",
    description = "",
    category = "Foraging",
    configKey = "shiny_critter_color",
    subcategory = "Boxes",
    dependsOn = BoxShinyCritters,
)