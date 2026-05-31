package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.onChatMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.ambient.Bat

object BatESP : SwitchFeature (
    name = "Bat ESP",
    description = "",
    category = "Dungeons",
    configKey = "bat_esp",
    subcategory = "Clear"
) {
    @Volatile var inF4Boss = false
    private var lastLevel: ClientLevel? = null

    init {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!")) {
                inF4Boss = true
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = Meridian.mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inF4Boss = false
            }
        })

        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled || inF4Boss) return@register
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
                ESP.drawBox(ctx, bat, w = 0.5, h = 1.0, wz = 0.5, yOffset = 0.0, argb = BatESPColor.color)
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
            if (!enabled || BatESP.inF4Boss) return@register
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
                    ESP.drawTracer(ctx, p.x, p.y, p.z, BatESPColor.color)
                }
            }
        }
    }
}

object BatESPColor : ColorFeature(
    name = "Bat Color",
    description = "",
    category = "Dungeons",
    configKey = "bat_color",
    subcategory = "Clear",
    dependsOn = BatESP,
)