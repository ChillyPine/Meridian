package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.P2State
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object ArchCritChecker : SwitchFeature(
    name = "Archer Crit Display",
    description = "Allows you to check your current crit during P2.\nMust use explosive arrow during P2 for math to work.",
    category = "Dungeons",
    configKey = "arch_high_crit_checker",
    subcategory = "P2",
) {
    private val damageRegex = Regex("""Your Explosive Shot hit (\d+) enem(?:y|ies) for ([\d,]+\.?\d*) damage\.""")
    private var highestCrit: Double = 0.0
    private var missedShot: Boolean = false
    private var wasInP2: Boolean = false

    private const val ONE_B = 1_000_000_000.0
    private const val TWO_B = 2_000_000_000.0

    private val element = object : HudElement(
        id = "arch_high_crit_checker_hud",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.6f,
    ) {
        override val shadow = true

        override fun content(): List<String> {
            if (!isActive() || !P2State.inP2) return emptyList()

            val display = when {
                missedShot -> "§c§lMissed Shot"
                highestCrit <= 0.0 -> "§c§lUse Explo Arrow"
                highestCrit < ONE_B -> "§c§l${"%,.1f".format(highestCrit)}"
                highestCrit < TWO_B -> "§e§l${"%,.1f".format(highestCrit)}"
                else -> "§a§l${"%,.1f".format(highestCrit)}"
            }
            return listOf("§b§lHigh Crit: §f§l$display")
        }

        override fun preview(): List<String> = listOf("§b§lHigh Crit: §f§lNULL")
    }

    init {
        HudManager.register(element)
        // horrible ass way of reseting the vals
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val inP2 = P2State.inP2
            if (inP2 && !wasInP2) {
                highestCrit = 0.0
                missedShot = false
            }
            wasInP2 = inP2
        })
        // math and shit
        onChat { text, _, _ ->
            if (!P2State.inP2) return@onChat

            damageRegex.find(text)?.let { match ->
                val enemies = match.groupValues[1].toIntOrNull() ?: return@let
                val damage = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: return@let

                if (enemies == 0) {
                    missedShot = true
                    return@let
                }

                missedShot = false
                val perEnemy = damage / enemies
                if (perEnemy > highestCrit) {
                    highestCrit = perEnemy
                }
            }
        }
    }
}