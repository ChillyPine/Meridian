package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.onChatMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.boss.wither.WitherBoss

object BoxGoldor : SwitchFeature(
    name = "Box Goldor",
    description = "Boxes Goldor's §lhitbox §rduring P2.",
    category = "Dungeons",
    configKey = "box_goldor",
    subcategory = "P3",
) {
    @Volatile
    private var goldorPhase = false
    private var lastLevel: ClientLevel? = null

    init {
        onChatMessage { text, _, _ ->
            when (text) {
                "[BOSS] Goldor: Who dares trespass into my domain?" -> goldorPhase = true
                "[BOSS] Necron: You went further than any human before, congratulations." -> goldorPhase = false
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = Meridian.mc.level
            if (level !== lastLevel) {
                lastLevel = level
                goldorPhase = false
            }
        })

        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled || !goldorPhase) return@register
            val level = Meridian.mc.level ?: return@register

            val storm = level.entitiesForRendering()
                .firstOrNull { it is WitherBoss && !it.isInvisible } as? WitherBoss ?: return@register

            ESP.drawFilled(ctx, storm, argb = GoldorColor.color)
        }
    }
}

object GoldorColor : ColorFeature(
    name = "Goldor Color",
    description = "",
    category = "Dungeons",
    configKey = "goldor_color",
    subcategory = "P3",
    dependsOn = BoxGoldor,
)