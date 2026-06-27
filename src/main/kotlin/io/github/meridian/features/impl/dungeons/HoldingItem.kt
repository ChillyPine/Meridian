package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.P5State
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
        onTick(DungeonState.state) {
            if (hasItem("Energy Crystal")) {
                mc.gui.setTimes(0, 5, 0)
                mc.gui.setTitle(Component.literal("§cHolding Crystal"))
                mc.gui.setSubtitle(Component.empty())
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
        onTick(P5State.state) {
            if (hasItem("Relic")) {
                mc.gui.setTimes(0, 5, 0)
                mc.gui.setTitle(Component.empty())
                mc.gui.setSubtitle(Component.literal("§cHolding Relic"))
            }
        }
    }
}