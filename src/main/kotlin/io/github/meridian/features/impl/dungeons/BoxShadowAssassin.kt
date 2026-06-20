package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.player.RemotePlayer

object BoxShadowAssassins : SwitchFeature(
    name = "Box Shadow Assassins",
    description = "",
    category = "Dungeons",
    configKey = "box_shadow_assassins",
    subcategory = "Clear",
)  {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is RemotePlayer) continue
                if (!ent.name.string.contains("Shadow Ass")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 1.8, wz = 0.6, argb = ShadowAssassinColor.color)
            }
        }
    }
}

object ShadowAssassinColor : ColorFeature(
    name = "Shadow Assassin Color",
    description = "",
    category = "Dungeons",
    configKey = "shadow_assassin_color",
    subcategory = "Clear",
    dependsOn = BoxShadowAssassins
)