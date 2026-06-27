package io.github.meridian.features.impl.events

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.decoration.ArmorStand

private val primalFearNames = listOf(
    "Primal Fear",
    "Commitment Phobia",
    "Darkness Shade",
    "Deadline",
    "Math Teacher",
    "Public Speaking Demon",
    "Vegan Crawler"
)

object BoxPrimalFears : SwitchFeature(
    name = "Box Primal Fears",
    description = "",
    category = "Events",
    configKey = "box_primal_fears",
    subcategory = "The Great Spook",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val playerIGN = Meridian.mc.player?.gameProfile?.name ?: return@onRender
            val tag = "Spawned by: $playerIGN"
            val ents = level.entitiesForRendering().toList()
            for (ent in ents) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains(tag)) continue
                val owned = ents.any { other ->
                    if (other === ent) return@any false
                    val n = other.customName?.string ?: return@any false
                    primalFearNames.any { n.contains(it) } && other.distanceTo(ent) < 1.0
                }
                if (!owned) continue
                ESP.drawBox(ctx, ent, w = 1.0, h = 2.0, wz = 1.0, yOffset = -1.7, argb = 0xFF9980E6.toInt())
            }
        }
    }
}