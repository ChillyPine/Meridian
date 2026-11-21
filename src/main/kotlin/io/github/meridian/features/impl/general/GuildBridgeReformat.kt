package io.github.meridian.features.impl.general

// Guild > [VIP] 2BridgeD: [D] DwnInFraggleRock: GET MESSAGE FORMAT TRUE
//   -> Discord > DwnInFraggleRock: GET MESSAGE FORMAT TRUE

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.simulateGameMessage

object GuildBridgeReformat : SwitchFeature(
    name = "Reformat Guild Bridge Messages",
    description = "Reformats 2_B_Determined's guild bridge messages for readability.",
    category = "General",
    configKey = "guild_bridge_reformat",
    subcategory = "Miscellaneous",
) {
    private const val BOT_NAME = "2BridgeD"

    private val formattingRegex = Regex("§.")

    private val discordRegex = Regex(
        """Guild > (?:\[[^\]]+] )?${Regex.escape(BOT_NAME)}(?: \[[^\]]+])?: \[D] ([^:]+): (.*)"""
    )

    init {
        blockChatRaw { message ->
            val plain = message.string.replace(formattingRegex, "").trim()

            val match = discordRegex.find(plain) ?: return@blockChatRaw false
            val (ign, text) = match.destructured

            simulateGameMessage("§9Discord §f> §b${ign.trim()}§f: $text")
            true
        }
    }
}