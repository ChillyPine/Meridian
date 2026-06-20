package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ChatBlocker

object BlockPFWarning : SwitchFeature (
    name = "Block Party Finder Warning Message",
    description = "",
    category = "Dungeons",
    configKey = "block_pf_warning",
    subcategory = "Miscellaneous",
) {
    init {
        ChatBlocker.register({ enabled }, "  Clicking sketchy links can result in your account")
        ChatBlocker.register( { enabled }, "  being stolen!")
        ChatBlocker.register( { enabled }, Regex("^   $"))
        ChatBlocker.register( { enabled }, "  Link looks suspicious? - Don't click it!")
    }
}