package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.hasItem
import io.github.meridian.utils.modMessage
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object BottleFull : SwitchFeature(
    name = "Bottle Full",
    description = "Notifies you of filled bottles after a dungeon",
    category = "Dungeons",
    configKey = "bottle_full",
    subcategory = "Miscellaneous",
) {
    private val TEAM_SCORE = Regex("^ *Team Score: (\\d+) \\(([\\w+]{1,2})\\)$")

    init {
        onChat { text, _, _ ->
            if (!TEAM_SCORE.matches(text)) return@onChat

            val bottles = mapOf(
                "Thunder in a Bottle" to "Your Thunder Bottle is §aFull! §eNice!",
                "Storm in a Bottle" to "Your Storm Bottle is §aFull! §dImpressive!",
                "Hurricane in a Bottle" to "Your Hurricane Bottle is §aFull! §6WOW!",
            )

            bottles.forEach { (item, message) ->
                if (hasItem(item)) {
                    TickScheduler.schedule(5, serverTick = false) {
                        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0f))
                        modMessage(message)
                    }
                }
            }
        }
    }
}
