package io.github.meridian.features.impl.mining

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.decoration.ArmorStand

// Butterfly, Corleone, Golden Goblin, Diamond Goblin, Key Guardian
object BoxButterflies : SwitchFeature(
    name = "Box Butterflies",
    description = "",
    category = "Mining",
    configKey = "box_butterflies",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
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
    subcategory = "Boxes",
    dependsOn = BoxButterflies,
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
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

object BoxCorleone : SwitchFeature(
    name = "Box Corleone",
    description = "",
    category = "Mining",
    configKey = "box_corleone",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Corleone")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFF00FF.toInt())
            }
        }
    }
}

object BoxGoldenGoblins : SwitchFeature(
    name = "Box Golden Goblins",
    description = "",
    category = "Mining",
    configKey = "box_golden_goblins",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Golden Goblin")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFFFF00.toInt())
            }
        }
    }
}

object BoxDiamondGoblins : SwitchFeature(
    name = "Box Diamond Goblins",
    description = "",
    category = "Mining",
    configKey = "box_diamond_goblins",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Diamond Goblin")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFF00FFFF.toInt())
            }
        }
    }
}

object BoxKeyGuardians : SwitchFeature(
    name = "Box Key Guardians",
    description = "",
    category = "Mining",
    configKey = "box_key_guardians",
    subcategory = "Boxes",
) {
    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                if (!name.contains("Key Guardian")) continue
                ESP.drawBox(ctx, ent, w = 0.6, h = 2.0, wz = 0.6, yOffset = -2.2, argb = 0xFFFF0000.toInt())
            }
        }
    }
}