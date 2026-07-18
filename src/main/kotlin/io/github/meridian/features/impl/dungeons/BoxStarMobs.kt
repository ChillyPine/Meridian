package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand

// Star mobs inc LAs and FAs
object BoxStarMobs : SwitchFeature(
    name = "Box Star Mobs",
    description = "",
    category = "Dungeons",
    configKey = "box_star_mobs",
    subcategory = "Clear",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                if (ent.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                val name = ent.customName?.string ?: continue
                if ((name.contains("✯") && !name.startsWith("✯")) && name.endsWith("❤") && !name.endsWith("✯") && !name.contains("Fel")) {
                    // The real mob is armorStandId - 1 (-3 for withermancers). If it's dead/gone,
                    // the stand can linger — skip so we don't box a mob that isn't there.
                    val mobId = ent.id - if (name.contains("Withermancer")) 3 else 1
                    val mob = level.getEntity(mobId) as? LivingEntity
                    if (mob == null || mob.isDeadOrDying || mob.isRemoved) continue
                    ESP.drawBox(ctx, ent, w = 0.75, h = 2.0, wz = 0.75, yOffset = -2.2, argb = StarMobColor.color)
                }
            }
        }
    }
}

object StarMobColor : ColorFeature(
    name = "Star Mob Color",
    description = "",
    category = "Dungeons",
    configKey = "star_mob_color",
    subcategory = "Clear",
    dependsOn = BoxStarMobs,
)

object BoxFels : SwitchFeature(
    name = "Box Fels",
    description = "",
    category = "Dungeons",
    configKey = "box_fels",
    subcategory = "Clear",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                if (ent.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                val name = ent.customName?.string ?: continue
                if (name.contains("Fel") && name.contains("✯")) {
                    val mob = level.getEntity(ent.id - 1) as? LivingEntity
                    if (mob == null || mob.isDeadOrDying || mob.isRemoved) continue
                    ESP.drawBox(ctx, ent, w = 0.6, h = 2.9, wz = 0.6, yOffset = -3.0, argb = FelColor.color)
                }
            }
        }
    }
}

object FelColor : ColorFeature(
    name = "Fel Color",
    description = "",
    category = "Dungeons",
    configKey = "fel_color",
    subcategory = "Clear",
    dependsOn = BoxFels,
)