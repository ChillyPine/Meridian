package io.github.meridian.features.impl.mining

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.world.entity.decoration.ArmorStand

// Butterfly, Corleone, Golden Goblin, Diamond Goblin, Key Guardian
object ButterflyESP : SwitchFeature(
    name = "Butterfly ESP",
    description = "",
    category = "Mining",
    configKey = "butterfly_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Butterfly")) continue
                ESP.drawBox(ctx, ent, w = 1.0, h = 1.0, wz = 1.0, yOffset = -1.0, argb = 0xFF00FFFF.toInt())
            }
        }
    }
}

object ButterflyTracer : SwitchFeature(
    name = "Butterfly Tracer",
    description = "",
    category = "Mining",
    configKey = "butterfly_tracer",
    subcategory = "ESPs",
    dependsOn = ButterflyESP,
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            val pt = Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Butterfly")) continue
                val p = ent.getPosition(pt)
                ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, 0xFF00FFFF.toInt())
            }
        }
    }
}

object CorleoneESP : SwitchFeature(
    name = "Corleone ESP",
    description = "",
    category = "Mining",
    configKey = "corleone_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Corleone")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFF00FF.toInt())
            }
        }
    }
}

object GoldenGoblinESP : SwitchFeature(
    name = "Golden Goblin ESP",
    description = "",
    category = "Mining",
    configKey = "golden_goblin_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Golden Goblin")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFFFF00.toInt())
            }
        }
    }
}

object DiamondGoblinESP : SwitchFeature(
    name = "Diamond Goblin ESP",
    description = "",
    category = "Mining",
    configKey = "diamond_goblin_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Diamond Goblin")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFF00FFFF.toInt())
            }
        }
    }
}

object KeyGuardianESP : SwitchFeature(
    name = "Key Guardian ESP",
    description = "",
    category = "Mining",
    configKey = "key_guardian_esp",
    subcategory = "ESPs",
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Key Guardian")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFF0000.toInt())
            }
        }
    }
}