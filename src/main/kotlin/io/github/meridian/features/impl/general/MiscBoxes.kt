package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.zombie.Zombie
import java.util.Optional
import net.minecraft.client.player.RemotePlayer
import java.util.UUID

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

object BoxRunicMobs : SwitchFeature(
    name = "Box Runic Mobs",
    description = "",
    category = "General",
    configKey = "box_runic_mobs",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
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
    subcategory = "Boxes",
    dependsOn = BoxRunicMobs
) {
    init {
        onRender { ctx ->
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
    description = "Color for Runic Mob Boxes & Tracer",
    category = "General",
    configKey = "runic_mob_color",
    subcategory = "Boxes",
    dependsOn = BoxRunicMobs,
)

object BoxMatchos : SwitchFeature(
    name = "Box Matchos",
    description = "",
    category = "General",
    configKey = "box_matchos",
    subcategory = "Boxes",
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Matcho")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = MatchoColor.color)
            }
        }
    }
}

object MatchoColor : ColorFeature(
    name = "Matcho Color",
    description = "",
    category = "General",
    configKey = "matcho_color",
    subcategory = "Boxes",
    dependsOn = BoxMatchos,
)

object BoxOldWolves : SwitchFeature(
    name = "Box Old Wolves",
    description = "",
    category = "General",
    configKey = "box_old_wolves",
    subcategory = "Boxes",
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Old Wolf")) continue
                ESP.drawBox(ctx, ent, w = 1.3, h = 1.0, wz = 1.0, yOffset = -1.0, argb = OldWolfColor.color)
            }
        }
    }
}

object OldWolfColor : ColorFeature(
    name = "Old Wolf Color",
    description = "",
    category = "General",
    configKey = "old_wolf_color",
    subcategory = "Boxes",
    dependsOn = BoxOldWolves,
)

object BoxRats : SwitchFeature(
    name = "Box Rats",
    description = "",
    category = "General",
    configKey = "box_rats",
    subcategory = "Boxes",
) {
    private const val CHECK_RADIUS = 3.0
    private val blockerNames = listOf("armadillo", "wraith", "watcher")
    private val nonRats = HashSet<Int>()

    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register

            val blockers = ArrayList<ArmorStand>()
            val zombies = ArrayList<Zombie>()
            val loadedIds = HashSet<Int>()
            for (ent in level.entitiesForRendering()) {
                loadedIds.add(ent.id)
                if (ent is ArmorStand) {
                    val name = ent.customName?.string?.lowercase() ?: continue
                    if (blockerNames.any { name.contains(it) }) blockers += ent
                } else if (ent is Zombie && ent.isInvisible) {
                    zombies += ent
                }
            }
            nonRats.retainAll(loadedIds)

            for (ent in zombies) {
                if (ent.id !in nonRats && blockers.any { bs ->
                        val dx = ent.x - bs.x
                        val dy = ent.y - bs.y
                        val dz = ent.z - bs.z
                        dx * dx + dy * dy + dz * dz <= CHECK_RADIUS * CHECK_RADIUS
                    }) {
                    nonRats.add(ent.id)
                }
                if (ent.id in nonRats) continue
                ESP.drawBox(ctx, ent, w = 0.9, h = 0.7, wz = 0.9, argb = 0xFFFFF300.toInt())
            }
        }
    }
}

// Replayz
const val targetUUID = "49180e88-3636-4303-85d4-5a7bcad13bc1"
object BoxFemboys : SwitchFeature(
    name = "Box Femboys",
    description = "For the gayest femboy of them all :3",
    category = "General",
    configKey = "box_femboys",
    subcategory = "Boxes",
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is RemotePlayer) continue
                if (ent.uuid != UUID.fromString(targetUUID)) continue
                ESP.drawBox(ctx, ent, w = 1.0, h = 2.0, wz = 0.6, argb = 0xFF990000.toInt())
            }
        }
    }
}