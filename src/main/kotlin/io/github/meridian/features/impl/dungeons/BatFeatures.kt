package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.F4State
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.world.entity.ambient.Bat

object BoxBats : SwitchFeature (
    name = "Box Bats",
    description = "",
    category = "Dungeons",
    configKey = "box_bats",
    subcategory = "Clear"
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled || F4State.inF4Boss || !DungeonState.inDungeon) return@register
            val level = Meridian.mc.level ?: return@register
            val player = Meridian.mc.player ?: return@register
            val bats = level.entitiesForRendering().filterIsInstance<Bat>()
            for (bat in bats) {
                if (bat.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                // Fuck hypixel wither/blood doors
                val nearbyBat = bats.any { other ->
                    other.id != bat.id && other.distanceToSqr(bat.x, bat.y, bat.z) <= 3.0 * 3.0
                }
                if (nearbyBat) continue
                ESP.drawBox(ctx, bat, w = 0.5, h = 1.0, wz = 0.5, yOffset = 0.0, argb = BoxBatsColor.color)
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
    dependsOn = BoxBats,
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled || F4State.inF4Boss || !DungeonState.inDungeon) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is Bat) continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                val bats = level.entitiesForRendering().filterIsInstance<Bat>()
                for (bat in bats) {
                    // Fuck hypixel wither/blood doors
                    val nearbyBat = bats.any { other ->
                        other.id != bat.id && other.distanceToSqr(bat.x, bat.y, bat.z) <= 3.0 * 3.0
                    }
                    if (nearbyBat) continue
                    ESP.drawTracer(ctx, p.x, p.y, p.z, BoxBatsColor.color)
                }
            }
        }
    }
}

object BoxBatsColor : ColorFeature(
    name = "Bat Color",
    description = "",
    category = "Dungeons",
    configKey = "bat_color",
    subcategory = "Clear",
    dependsOn = BoxBats,
)
