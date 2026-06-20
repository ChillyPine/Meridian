package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.decoration.ArmorStand

object BoxDoorKeys : SwitchFeature(
    name = "Box Door Keys",
    description = "",
    category = "Dungeons",
    configKey = "box_door_keys",
    subcategory = "Clear",
) {
    private val seenKeys = mutableSetOf<Int>()

    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register

            val currentKeys = mutableSetOf<Int>()

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                if (name.contains("Wither Key") || name.contains("Blood Key")) {
                    currentKeys.add(ent.id)
                    ESP.drawBox(ctx, ent, w = 0.75, h = 1.0, wz = 0.75, yOffset = +1.0, argb = DoorKeyColor.color)
                    ESP.drawTracer(ctx, p.x, p.y + 1.5, p.z, DoorKeyColor.color)

                    if (PlaySoundOnKeyDrop.enabled && ent.id !in seenKeys) {
                        Meridian.mc.soundManager.play(
                            SimpleSoundInstance.forUI(SoundEvents.VAULT_OPEN_SHUTTER, 2.0f)
                        )
                    }
                }
            }

            seenKeys.retainAll(currentKeys)
            seenKeys.addAll(currentKeys)
        }
    }
}
object DoorKeyColor : ColorFeature(
    name = "Door Key Color",
    description = "",
    category = "Dungeons",
    configKey = "door_key_color",
    subcategory = "Clear",
    dependsOn = BoxDoorKeys,
)

object PlaySoundOnKeyDrop : SwitchFeature(
    name = "Play Sound on Key Drop",
    description = "",
    category = "Dungeons",
    configKey = "key_drop_sound",
    subcategory = "Clear",
    dependsOn = BoxDoorKeys,
)