package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.DungeonState

object BlockPFWarning : SwitchFeature (
    name = "Block Party Finder Warning Message",
    description = "",
    category = "Dungeons",
    configKey = "block_pf_warning",
    subcategory = "Miscellaneous",
) {
    init {
        blockChat("  Clicking sketchy links can result in your account")
        blockChat("  being stolen!")
        blockChat(Regex("^   $"))
        blockChat("  Link looks suspicious? - Don't click it!")
    }
}

object BlockBlessings : SwitchFeature (
    name = "Block Blessing Messages",
    description = "",
    category = "Dungeons",
    configKey = "block_blessings",
    subcategory = "Miscellaneous",
) {
    init {
        blockChat(Regex("has obtained Blessing of \\w+!$"), DungeonState.state)
        blockChat(Regex("^DUNGEON BUFF! "), DungeonState.state)
        blockChat(Regex("^\\s*(?:Also )?[Gg]ranted you \\+"), DungeonState.state)
    }
}

object BlockSuperboom : SwitchFeature (
    name = "Block Superboom Pickup Message",
    description = "",
    category = "Dungeons",
    configKey = "block_superboom",
    subcategory = "Miscellaneous",
) {
    init {
        blockChat(Regex("(\\S+) has obtained Superboom TNT!$"), DungeonState.state)
    }
}

object BlockReviveStone : SwitchFeature (
    name = "Block Revive Stone Pickup Message",
    description = "",
    category = "Dungeons",
    configKey = "block_revive_stone",
    subcategory = "Miscellaneous",
) {
    init {
        blockChat(Regex("(\\S+) has obtained Revive Stone!$"), DungeonState.state)
    }
}

object BlockTrap : SwitchFeature (
    name = "Block Trap Damage Messages",
    description = "",
    category = "Dungeons",
    configKey = "block_trap",
    subcategory = "Miscellaneous",
) {
    init {
        blockChat(Regex("^The .+ hit you for .+ damage!$"), DungeonState.state)
    }
}