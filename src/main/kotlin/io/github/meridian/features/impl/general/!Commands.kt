package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.onChatMessage
import io.github.meridian.utils.sendCommand
import kotlin.math.floor
import net.minecraft.util.StringUtil

private const val DT_ART =
    "゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜ ゛゜゛███゛゜█████゛゜█゛゜█゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜ ゜゛゜█゜゛█゛゜゛█゜゛゜゛█゜゛█゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛ ゛゜゛█゛゜█゜゛゜█゛゜゛゜█゛゜█゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜ ゜゛゜█゜゛█゛゜゛█゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛゜゛ ゛゜゛███゛゜゛゜█゛゜゛゜█゛゜█゜゛゜゛゜"

private val dtRegex = Regex("^Party > .+: !dt$")
private val coordsRegex = Regex("^Party > .+: !coords$")

// !dt, !coords, !ptme, !allinv, !warp
object DTCommand : SwitchFeature(
    name = "!dt",
    description = "Sends ASCII art of DT in chat.",
    category = "General",
    configKey = "dt_command",
    subcategory = "! Commands",
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!dtRegex.matches(text)) return@onChatMessage
            sendCommand("pc " + StringUtil.filterText(DT_ART))
        }
    }
}

object CoordsCommand : SwitchFeature(
    name = "!coords",
    description = "Sends your coords in party chat",
    category = "General",
    configKey = "coords_command",
    subcategory = "! Commands",
) {
    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!coordsRegex.matches(text)) return@onChatMessage
            val p = Meridian.mc.player ?: return@onChatMessage
            val x = floor(p.x).toInt()
            val y = floor(p.y).toInt()
            val z = floor(p.z).toInt()
            sendCommand("pc x: $x y: $y z: $z")
        }
    }
}