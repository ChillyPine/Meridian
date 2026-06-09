package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.hasItem
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

// Notifies of storm bottle full (and variants)
object BottleFull : SwitchFeature(
    name = "Bottle Full",
    description = "Notifies you of filled bottles after a dungeon",
    category = "Dungeons",
    configKey = "bottle_full",
    subcategory = "Miscellaneous",
) {
    private val TEAM_SCORE = Regex("^ *Team Score: (\\d+) \\(([\\w+]{1,2})\\)$")

    init {

        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!TEAM_SCORE.matches(text)) return@onChatMessage

            val bottles = mapOf(
                "Thunder in a Bottle"    to "Your Thunder Bottle is §aFull! §eNice!",
                "Storm in a Bottle"      to "Your Storm Bottle is §aFull! §dImpressive!",
                "Hurricane in a Bottle"  to "Your Hurricane Bottle is §aFull! §6WOW!",
            )

            bottles.forEach { (item, message) ->
                if (hasItem(item))
                    TickScheduler.schedule(5, serverTick = false) {
                        mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0f))
                        modMessage(message)
                    }
            }
        }

    }
}
