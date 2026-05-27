package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.decoration.ArmorStand

// Star mobs inc LAs and FAs
object StarMobESP : SwitchFeature(
    name = "Star Mob ESP",
    description = "",
    category = "Dungeons",
    configKey = "star_mob_esp",
    subcategory = "Clear",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (name.contains("✯") && !name.endsWith("✯") && !name.contains("Fel")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = StarMobColor.color)
                }
            }
        }
    }
}

object StarMobColor : ColorFeature(
    name = "Star Mob Color",
    description = "",
    category = "Dungeons",
    configKey = "star_mob_color",
    subcategory = "Clear",
    dependsOn = StarMobESP,
)

object FelESP : SwitchFeature(
    name = "Fel ESP",
    description = "",
    category = "Dungeons",
    configKey = "fel_esp",
    subcategory = "Clear",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (name.contains("Fel") && name.contains("✯")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.9, wz = 0.6, yOffset = -3.0, argb = FelColor.color)
                }
            }
        }
    }
}

object FelColor : ColorFeature(
    name = "Fel Color",
    description = "",
    category = "Dungeons",
    configKey = "fel_color",
    subcategory = "Clear",
    dependsOn = FelESP,
)