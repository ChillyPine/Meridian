package io.github.meridian.features.impl.general

import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ChatBlocker

object BlockBlocksInWay : SwitchFeature (
    name = "Block \"There are blocks in the way!\"",
    description = "",
    category = "General",
    configKey = "block_blocks_in_way",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, "There are blocks in the way") }
}

object BlockGEXP : SwitchFeature (
    name = "Block \"You earned # GEXP + # Event EXP from playing Skyblock!\"",
    description = "",
    category = "General",
    configKey = "block_gexp",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, Regex("You earned .* GEXP")) }
}

object BlockProfileID : SwitchFeature (
    name = "Block Profile ID Message",
    description = "",
    category = "General",
    configKey = "block_profile_id",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, "Profile ID:") }
}

object BlockProfileProduce : SwitchFeature (
    name = "Block Profile Message",
    description = "Blocks \"You are playing on [FRUIT] profile.\"",
    category = "General",
    configKey = "block_profile_produce",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, Regex("You are playing on \\S+ profile")) }
}

object BlockHOTF : SwitchFeature (
    name = "Block HOTF Lottery Message",
    description = "Blocks \"You can disable this message by toggling Lottery in your /hotf!\"",
    category = "General",
    configKey = "block_hotf",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, "toggling Lottery in your /hotf") }
}

object BlockDiscord : SwitchFeature (
    name = "Block Discord Warning Message",
    description = "",
    category = "General",
    configKey = "block_discord",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, "discord.gg/hypixel") }
}