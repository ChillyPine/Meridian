package io.github.meridian.features.impl.general

import io.github.meridian.features.SwitchFeature

object BlockBlocksInWay : SwitchFeature (
    name = "Block \"There are blocks in the way!\"",
    description = "",
    category = "General",
    configKey = "block_blocks_in_way",
    subcategory = "Chat Blockers",
)

object BlockGEXP : SwitchFeature (
    name = "Block \"You earned # GEXP + # Event EXP from playing Skyblock!\"",
    description = "",
    category = "General",
    configKey = "block_gexp",
    subcategory = "Chat Blockers",
)

object BlockProfileID : SwitchFeature (
    name = "Block Profile ID Message",
    description = "",
    category = "General",
    configKey = "block_profile_id",
    subcategory = "Chat Blockers",
)

object BlockProfileProduce : SwitchFeature (
    name = "Block Profile Message",
    description = "Blocks \"You are playing on [FRUIT] profile.\"",
    category = "General",
    configKey = "block_profile_produce",
    subcategory = "Chat Blockers",
)

object BlockHOTF : SwitchFeature (
    name = "Block HOTF Lottery Message",
    description = "Blocks \"You can disable this message by toggling Lottery in your /hotf!\"",
    category = "General",
    configKey = "block_hotf",
    subcategory = "Chat Blockers",
)

object BlockDiscord : SwitchFeature (
    name = "Block Discord Warning Message",
    description = "",
    category = "General",
    configKey = "block_discord",
    subcategory = "Chat Blockers",
)