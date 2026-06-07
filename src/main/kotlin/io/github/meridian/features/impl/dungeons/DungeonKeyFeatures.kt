package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian
import io.github.meridian.features.ColorFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.decoration.ArmorStand

object DoorKeyESP : SwitchFeature(
    name = "Door Key ESP",
    description = "",
    category = "Dungeons",
    configKey = "door_key_esp",
    subcategory = "Clear",
) {
    private val seenKeys = mutableSetOf<Int>()

    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register

            val currentKeys = mutableSetOf<Int>()

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                val name = ent.customName?.string ?: continue
                val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                if (name.contains("Wither Key") || name.contains("Blood Key")) {
                    currentKeys.add(ent.id)
                    ESP.drawBox(ctx, ent, w = 0.75, h = 1.0, wz = 0.75, yOffset = +1.0, argb = DoorKeyESPColor.color)
                    ESP.drawTracer(ctx, p.x, p.y - 1.0, p.z, DoorKeyESPColor.color)

                    if (PlaySoundOnKeyDrop.enabled && ent.id !in seenKeys) {
                        Meridian.mc.soundManager.play(
                            SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 2.0f)
                        )
                    }
                }
            }

            seenKeys.retainAll(currentKeys)
            seenKeys.addAll(currentKeys)
        }
    }
}
object DoorKeyESPColor : ColorFeature(
    name = "Door Key ESP Color",
    description = "",
    category = "Dungeons",
    configKey = "door_key_color",
    subcategory = "Clear",
    dependsOn = DoorKeyESP,
)

object PlaySoundOnKeyDrop : SwitchFeature(
    name = "Play Sound on Key Drop",
    description = "",
    category = "Dungeons",
    configKey = "key_drop_sound",
    subcategory = "Clear",
    dependsOn = DoorKeyESP,
)