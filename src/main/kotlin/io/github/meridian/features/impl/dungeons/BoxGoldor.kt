package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.BossState
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.boss.wither.WitherBoss

object BoxGoldor : SwitchFeature(
    name = "Box Goldor",
    description = "Boxes Goldor's §lhitbox §rduring P3.",
    category = "Dungeons",
    configKey = "box_goldor",
    subcategory = "P3",
) {


    init {
        onRender { ctx ->
            if (!BossState.inP3) return@onRender
            val level = Meridian.mc.level ?: return@onRender

            val storm = level.entitiesForRendering()
                .firstOrNull { it is WitherBoss && !it.isInvisible } as? WitherBoss ?: return@onRender
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