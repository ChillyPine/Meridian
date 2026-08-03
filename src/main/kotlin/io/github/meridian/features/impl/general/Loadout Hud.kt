package io.github.meridian.features.impl.general

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager

object LoadoutHudFeature : SwitchFeature(
    name = "Loadout Display",
    description = "Displays 'Current loadout: X'",
    category = "General",
    configKey = "loadout_hud",
    subcategory = "Miscellaneous",
) {
    private var currentLoadout: String? = null
    private val equipRegex = Regex("^You equipped (.+)!$")
    private val element = object : HudElement(
        id = "loadout_display",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.6f,
    ) {
        override val shadow = true

        override fun content(): List<String> {
            val loadout = currentLoadout ?: "NULL"
            return listOf("§b§lCurrent loadout: §f§l$loadout")
        }

        override fun preview(): List<String> = listOf("§b§lCurrent loadout: §f§lNULL")
    }

    init {
        HudManager.register(element)

        onChat { text, _, _ ->
            equipRegex.find(text)?.let { match ->
                currentLoadout = match.groupValues[1]
            }
        }
    }
}