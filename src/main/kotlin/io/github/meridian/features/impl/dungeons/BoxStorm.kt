package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.onChatMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.boss.wither.WitherBoss

object BoxStorm : SwitchFeature(
    name = "Box Storm",
    description = "Boxes Storm's §lhitbox§r during P2.",
    category = "Dungeons",
    configKey = "box_storm",
    subcategory = "P2"
) {

    @Volatile private var stormPhase = false
    private var lastLevel: ClientLevel? = null

    init {
        onChatMessage { text, _, _ ->
            when (text) {
                "[BOSS] Storm: Pathetic Maxor, just like expected." -> stormPhase = true
                "[BOSS] Goldor: Who dares trespass into my domain?" -> stormPhase = false
            }
        }

        // see world change
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = Meridian.mc.level
            if (level !== lastLevel) {
                lastLevel = level
                stormPhase = false
            }
        })

        onRender { ctx ->
            if (!stormPhase) return@onRender
            val level = Meridian.mc.level ?: return@onRender

            // render that invisible dumbfucker
            val storm = level.entitiesForRendering()
                .firstOrNull { it is WitherBoss && !it.isInvisible } as? WitherBoss ?: return@onRender

            ESP.drawFilled(ctx, storm, argb = StormColor.color)
        }
    }
}

object StormColor : ColorFeature(
    name = "Storm Color",
    description = "",
    category = "Dungeons",
    configKey = "storm_color",
    subcategory = "P2",
    dependsOn = BoxStorm,
)