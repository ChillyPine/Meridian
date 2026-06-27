package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.types.SwitchFeature
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object BlockBlocksInWay : SwitchFeature (
    name = "Block \"There are blocks in the way!\"",
    description = "",
    category = "General",
    configKey = "block_blocks_in_way",
    subcategory = "Chat Blockers",
) {
    init { blockChat(Regex("^There are blocks in the way!$")) }
}

object BlockGEXP : SwitchFeature (
    name = "Block \"You earned # GEXP + # Event EXP from playing Skyblock!\"",
    description = "",
    category = "General",
    configKey = "block_gexp",
    subcategory = "Chat Blockers",
) {
    init { blockChat(Regex("^You earned .+ from playing SkyBlock!$")) }
}

object BlockProfileID : SwitchFeature (
    name = "Block Profile ID Message",
    description = "",
    category = "General",
    configKey = "block_profile_id",
    subcategory = "Chat Blockers",
) {
    init { blockChat(Regex("^Profile ID: .+$")) }
}

object BlockProfileProduce : SwitchFeature (
    name = "Block Profile Message",
    description = "Blocks \"You are playing on [FRUIT] profile.\"",
    category = "General",
    configKey = "block_profile_produce",
    subcategory = "Chat Blockers",
) {
    init { blockChat(Regex("You are playing on profile: *|You are playing on profile: * \\(Co-op\\)")) }
}

object BlockHOTFM : SwitchFeature (
    name = "Block HOTF Lottery and HOTM Sky Mall Message",
    description = "Blocks \"You can disable this message by toggling Lottery/Sky Mall in your /hotf/m!\"",
    category = "General",
    configKey = "block_hotfm",
    subcategory = "Chat Blockers",
) {
    init { blockChat(Regex("^You can disable this messaging by toggling (Lottery|Sky Mall) in your (/hotf!|/hotm!)$")) }
}

object BlockDiscord : SwitchFeature (
    name = "Block Discord Warning Message",
    description = "",
    category = "General",
    configKey = "block_discord",
    subcategory = "Chat Blockers",
) {
    private const val WARNING = "Please be mindful of Discord links in chat as they may pose a security risk"

    init {
        blockChatRaw { message ->
            if (WARNING !in message.string) return@blockChatRaw false
            val cleaned = stripWarning(message)
            if (cleaned != null && cleaned.string.isNotBlank()) {
                Meridian.mc.gui.chat.addClientSystemMessage(cleaned)
            }
            true
        }
    }

    private fun stripWarning(src: Component): Component? {
        val rebuilt: MutableComponent =
            MutableComponent.create(src.contents).setStyle(src.style)
        if (WARNING in rebuilt.string) return null
        val kept = mutableListOf<Component>()
        for (sib in src.siblings) {
            if (WARNING in sib.string) break
            kept += sib
        }
        while (kept.isNotEmpty() && kept.last().string.isBlank()) kept.removeAt(kept.size - 1)
        kept.forEach { rebuilt.append(it) }
        return rebuilt
    }
}

object BlockWatchdog : SwitchFeature (
    name = "Block Watchdog Intimidation Message",
    description = "Blocks \"Watchdog has banned 9,999 players in the past 7 days!\"",
    category = "General",
    configKey = "block_watchdog",
    subcategory = "Chat Blockers",
) {
    init {
        blockChat(Regex("^\\[WATCHDOG ANNOUNCEMENT\\]$"))
        blockChat(Regex("^Watchdog has banned .* players in the last 7 days\\.$"))
        blockChat(Regex("^Staff have banned an additional .* in the last 7 days\\.$"))
        blockChat(Regex("^Blacklisted modifications are a bannable offense!$"))
    }
}
