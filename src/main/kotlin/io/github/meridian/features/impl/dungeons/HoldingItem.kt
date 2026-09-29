package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.BossState
import io.github.meridian.utils.hasItem
import net.minecraft.network.chat.Component

// Holding crystal in p1, holding relic
object HoldingCrystal : SwitchFeature(
    name = "Holding Crystal",
    description = "Holding Crystal item",
    category = "Dungeons",
    configKey = "holding_crystal",
    subcategory = "P1",
) {
    init {
        onTick {
            if (hasItem("Energy Crystal") && BossState.inP1) {
                mc.gui.hud.setTimes(0, 5, 0)
                mc.gui.hud.setTitle(Component.literal("§cHolding Crystal"))
                mc.gui.hud.setSubtitle(Component.empty())
            }
        }
    }
}

object HoldingRelic : SwitchFeature(
    name = "Holding Relic",
    description = "Tells you if you are holding a relic during P5.",
    category = "Dungeons",
    configKey = "holding_relic",
    subcategory = "P5",
) {
    init {
        onTick {
            if (hasItem("Relic") && BossState.inP5) {
                mc.gui.hud.setTimes(0, 5, 0)
                mc.gui.hud.setTitle(Component.empty())
                mc.gui.hud.setSubtitle(Component.literal("§cHolding Relic"))
            }
        }
    }
}