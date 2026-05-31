package io.github.meridian.features.impl.farming

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.decoration.ArmorStand


object TrapperESP : SwitchFeature(
    name = "TrapperESP",
    description = "Unlike Skyhanni, mine works in the Oasis",
    category = "Farming",
    configKey = "trapper_esp",
    subcategory = "ESPs"
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                if (name.contains("Trackable")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFFFFFF.toInt())
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFFFFFFFF.toInt())
                } else if (name.contains("Untrackable")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFF00FF00.toInt())
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFF00FF00.toInt())
                } else if (name.contains("Undetected")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFF0051FF.toInt())
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFF0051FF.toInt())
                } else if (name.contains("Endangered")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFD400FF.toInt())
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFFD400FF.toInt())
                } else if (name.contains("Elusive")) {
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFFF200.toInt())
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFFFFF200.toInt())
                } else continue
            }
        }
    }
}