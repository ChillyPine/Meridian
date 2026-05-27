package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.Optional

// Runic ESP function
private fun Component.hasPurpleBracket(): Boolean {
    var found = false
    this.visit({ style, text ->
        if (style.color?.value == ChatFormatting.DARK_PURPLE.color && text.contains("[")) {
            found = true
        }
        Optional.empty<Any>()
    }, Style.EMPTY)
    return found
}

// Runic ESP function #2
private inline fun forEachRunicMob(block: (ArmorStand) -> Unit) {
    val level = Meridian.mc.level ?: return
    for (ent in level.entitiesForRendering()) {
        if (ent !is ArmorStand) continue
        val name = ent.customName ?: continue
        if (name.string.contains("Dragon")) continue
        if (!name.hasPurpleBracket()) continue
        block(ent)
    }
}

object RunicMobESP : SwitchFeature(
    name = "Runic Mob ESP",
    description = "",
    category = "General",
    configKey = "runic_mob_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            forEachRunicMob { ent ->
                ESP.drawBox(ctx, ent, w = 1.0, h = 1.0, wz = 1.0, yOffset = -1.0, argb = RunicMobColor.color)
            }
        }
    }
}

object RunicMobTracer : SwitchFeature(
    name = "Runic Mob Tracer",
    description = "",
    category = "General",
    configKey = "runic_mob_tracer",
    subcategory = "ESPs",
    dependsOn = RunicMobESP
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val pt = Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            forEachRunicMob { ent ->
                val p = ent.getPosition(pt)
                ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, RunicMobColor.color)
            }
        }
    }
}

object RunicMobColor : ColorFeature(
    name = "Runic Mob Color",
    description = "Color for Runic Mob ESP & Tracer",
    category = "General",
    configKey = "runic_mob_color",
    subcategory = "ESPs",
    dependsOn = RunicMobESP,
)

// Rat ESP

// Matcho ESP
object MatchoESP : SwitchFeature(
    name = "Matcho ESP",
    description = "",
    category = "General",
    configKey = "matcho_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Matcho")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = MatchoESPColor.color)
            }
        }
    }
}

object MatchoESPColor : ColorFeature(
    name = "Matcho ESP Color",
    description = "Color",
    category = "General",
    configKey = "matcho_esp_color",
    subcategory = "ESPs",
    dependsOn = MatchoESP,
)
// Player ESP


object OldWolfESP : SwitchFeature(
    name = "Old Wolf ESP",
    description = "",
    category = "General",
    configKey = "old_wolf_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Old Wolf")) continue
                ESP.drawBox(ctx, ent, w = 1.3, h = 1.0, wz = 1.0, yOffset = -1.0, argb = OldWolfESPColor.color)
            }
        }
    }
}

object OldWolfESPColor : ColorFeature(
    name = "Old Wolf ESP Color",
    description = "Color",
    category = "General",
    configKey = "old_wolf_esp_color",
    subcategory = "ESPs",
    dependsOn = OldWolfESP,
)