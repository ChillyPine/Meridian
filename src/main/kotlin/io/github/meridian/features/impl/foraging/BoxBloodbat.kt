package io.github.meridian.features.impl.foraging

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.Island
import io.github.meridian.utils.SkyblockLocation
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.ambient.Bat

object BoxBloodbat : SwitchFeature(
    name = "Box Bloodbats",
    description = "",
    category = "Safari",
    configKey = "box_bloodbat",
    subcategory = "Haunted"
) {
    private val HAUNTED = Identifier.parse("hypixel:haunted")

    init {
        onRender(SkyblockLocation.on(Island.SAFARI)) { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender
            if (!level.getBiome(player.blockPosition()).`is`(HAUNTED)) return@onRender
            val bats = level.entitiesForRendering().filterIsInstance<Bat>()
            for (bat in bats) {
                if (bat.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                ESP.drawBox(ctx, bat, w = 0.5, h = 1.0, wz = 0.5, yOffset = 0.0, argb = 0xFFFF0000.toInt())
            }
        }
    }
}