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
import net.minecraft.world.entity.monster.zombie.Zombie
import java.util.Optional
import net.minecraft.client.player.RemotePlayer
import java.util.UUID

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
            if (!isActive()) return@register
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
    name = "Matcho Color",
    description = "",
    category = "General",
    configKey = "matcho_color",
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
    name = "Old Wolf Color",
    description = "",
    category = "General",
    configKey = "old_wolf_color",
    subcategory = "ESPs",
    dependsOn = OldWolfESP,
)

object RatESP : SwitchFeature(
    name = "Rat ESP",
    description = "",
    category = "General",
    configKey = "rat_esp",
    subcategory = "ESPs",
) {
    private const val CHECK_RADIUS = 3.0
    // Named armor stands that mark a different invisible-zombie mob. A rat is an
    // invisible zombie with none of these nearby.
    private val blockerNames = listOf("armadillo", "wraith", "watcher")

    // Entity ids of invisible zombies confirmed to be a non-rat mob (they were
    // seen near an armadillo/wraith/watcher nametag). Nametag armor stands stop
    // being tracked at a shorter range than the zombie hitbox, so once we've
    // associated the two we keep excluding the zombie even after its nametag
    // unloads. Pruned to currently-loaded entities each frame so a reused id
    // (new entity / world change) doesn't inherit a stale tag.
    private val nonRats = HashSet<Int>()

    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register

            // Single pass: gather blocker nametags, candidate zombies, loaded ids.
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
            // Forget tags for zombies that have unloaded.
            nonRats.retainAll(loadedIds)

            for (ent in zombies) {
                // Tag (permanently, while loaded) any zombie currently near a blocker.
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
object FemboyESP : SwitchFeature(
    name = "Femboy ESP",
    description = "For the gayest femboy of them all :3",
    category = "General",
    configKey = "femboy_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
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