package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P2State
import net.minecraft.world.entity.boss.wither.WitherBoss

object BoxStorm : SwitchFeature(
    name = "Box Storm",
    description = "Boxes Storm's §lhitbox§r during P2.",
    category = "Dungeons",
    configKey = "box_storm",
    subcategory = "P2"
) {
    init {

        onRender { ctx ->
            if (!P2State.inP2) return@onRender
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