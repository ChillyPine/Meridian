package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.ambient.Bat

// Bat esp

object BatESP : SwitchFeature (
    name = "Bat ESP",
    description = "",
    category = "Dungeons",
    configKey = "bat_esp",
    subcategory = "Clear"
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            val player = Meridian.mc.player ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is Bat) continue
                if (ent.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                ESP.drawBox(ctx, ent, w = 0.5, h = 1.0, wz = 0.5, yOffset = 0.0, argb = BatESPColor.color)

            }
        }
    }
}

object BatTracer : SwitchFeature (
    name = "Bat Tracer",
    description = "",
    category = "Dungeons",
    configKey = "bat_tracer",
    subcategory = "Clear",
    dependsOn = BatESP,
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is Bat) continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                ESP.drawTracer(ctx, p.x, p.y, p.z, BatESPColor.color)
            }
        }
    }
}

object BatESPColor : ColorFeature(
    name = "Bat Color",
    description = "",
    category = "Dungeons",
    configKey = "bat_mob_color",
    subcategory = "Clear",
    dependsOn = BatESP,
)