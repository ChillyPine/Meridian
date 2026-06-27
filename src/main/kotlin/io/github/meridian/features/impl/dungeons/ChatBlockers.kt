package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature

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
