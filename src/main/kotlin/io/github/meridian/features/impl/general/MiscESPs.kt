package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.decoration.ArmorStand

/**
 * Box ESP for Old Wolf in The Park. Matches the armor stand whose
 * name contains "Old Wolf" and draws a 1.3 x 1 x 1 box at the wolf's
 * actual position (one block below the nametag).
 */
object OldWolfESP : SwitchFeature(
    name = "Old Wolf ESP",
    description = "Boxes the Old Wolf in The Park.",
    category = "General",
    configKey = "old_wolf_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Old Wolf")) continue
                ESP.drawBox(ctx, ent, w = 1.3, h = 1.0, wz = 1.0, yOffset = -1.0, argb = OldWolfESPColor.color)
            }
        }
    }
}

object OldWolfESPColor : ColorFeature(
    name = "Old Wolf ESP Color",
    description = "Color",
    category = "General",
    configKey = "old_wolf_esp_color",
    subcategory = "ESPs",
    dependsOn = OldWolfESP,
)