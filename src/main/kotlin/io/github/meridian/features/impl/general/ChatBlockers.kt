package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.ChatBlocker
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object BlockBlocksInWay : SwitchFeature (
    name = "Block \"There are blocks in the way!\"",
    description = "",
    category = "General",
    configKey = "block_blocks_in_way",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, Regex("^There are blocks in the way!$")) }
}

object BlockGEXP : SwitchFeature (
    name = "Block \"You earned # GEXP + # Event EXP from playing Skyblock!\"",
    description = "",
    category = "General",
    configKey = "block_gexp",
    subcategory = "Chat Blockers",
) {
    init {
        ChatBlocker.register({ enabled }, Regex("^You earned*+from playing SkyBlock!$"))
    }
}

object BlockProfileID : SwitchFeature (
    name = "Block Profile ID Message",
    description = "",
    category = "General",
    configKey = "block_profile_id",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, Regex("^Profile ID: .+$")) }
}

object BlockProfileProduce : SwitchFeature (
    name = "Block Profile Message",
    description = "Blocks \"You are playing on [FRUIT] profile.\"",
    category = "General",
    configKey = "block_profile_produce",
    subcategory = "Chat Blockers",
) {
    init {
        ChatBlocker.register({ enabled }, Regex("You are playing on profile: *|You are playing on profile: * \\(Co-op\\)"))
    }
}

object BlockHOTFM : SwitchFeature (
    name = "Block HOTF Lottery and HOTM Sky Mall Message",
    description = "Blocks \"You can disable this message by toggling Lottery/Sky Mall in your /hotf/m!\"",
    category = "General",
    configKey = "block_hotfm",
    subcategory = "Chat Blockers",
) {
    init { ChatBlocker.register({ enabled }, Regex("^You can disable this messaging by toggling (Lottery|Sky Mall) in your (/hotf!|/hotm!)$")) }
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
        // Hypixel appends the warning to the original chat message instead of
        // sending it separately, so a plain ALLOW_GAME veto would also drop the
        // user's "discord" line. Rebuild the component without the warning and
        // re-emit it.
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            if (!enabled || WARNING !in message.string) return@register true
            val cleaned = stripWarning(message)
            if (cleaned != null && cleaned.string.isNotBlank()) {
                Meridian.mc.gui.chat.addMessage(cleaned)
            }
            false
        }
    }

    private fun stripWarning(src: Component): Component? {
        val rebuilt: MutableComponent =
            MutableComponent.create(src.contents).setStyle(src.style)
        // If the warning lives in the root contents itself we can't cleanly
        // peel it off — bail and let the whole message be dropped.
        if (WARNING in rebuilt.string) return null
        val kept = mutableListOf<Component>()
        for (sib in src.siblings) {
            if (WARNING in sib.string) break
            kept += sib
        }
        // Drop trailing blank siblings (newlines/spaces left over from the
        // separator between the original message and the warning).
        while (kept.isNotEmpty() && kept.last().string.isBlank()) kept.removeAt(kept.size - 1)
        kept.forEach { rebuilt.append(it) }
        return rebuilt
    }
}