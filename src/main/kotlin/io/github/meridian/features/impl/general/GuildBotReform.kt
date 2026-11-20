package io.github.meridian.features.impl.general

// Guild > [VIP] 2BridgeD: [D] DwnInFraggleRock: GET MESSAGE FORMAT TRUE
//   -> Discord > DwnInFraggleRock: GET MESSAGE FORMAT TRUE

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.simulateGameMessage

object GuildBotReform : SwitchFeature(
    name = "Guild Message Reform",
    description = "Reformats 2b_Determined's guild bots messages to be actually readable",
    category = "General",
    configKey = "guild_bot_reform",
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

            // TEMP DEBUG: remove once it works
            if (plain.contains(BOT_NAME)) println("[GuildBotReform] raw='${message.string}' plain='$plain'")

            val match = discordRegex.find(plain) ?: return@blockChatRaw false
            val (ign, text) = match.destructured

            simulateGameMessage("§9Discord §f> §b${ign.trim()}§f: $text")
            true
        }
    }
}