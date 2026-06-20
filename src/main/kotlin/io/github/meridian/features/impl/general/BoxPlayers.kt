package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.types.DropdownFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.features.types.TextFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.player.RemotePlayer

private const val PLAYER_BOX_RADIUS = 75.0

object BoxPlayers : SwitchFeature(
    name = "Box Players",
    description = "",
    category = "General",
    configKey = "box_players",
    subcategory = "Boxes"
) {
    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            val self = Meridian.mc.player ?: return@register

            for (ent in level.entitiesForRendering()) {
                if (ent !is RemotePlayer) continue
                if (ent.distanceToSqr(self.x, self.y, self.z) > PLAYER_BOX_RADIUS * PLAYER_BOX_RADIUS) continue

                when (PlayerBoxMode.selectedOption) {
                    "All Players" -> {}
                    "Specific Players" -> if (!BoxSpecificPlayer.matches(ent.gameProfile.name)) continue
                    else -> continue
                }
                ESP.drawBox(ctx, ent, w = 1.0, h = 2.0, wz = 0.6, argb = 0xFF00B300.toInt())
            }
        }
    }
}

object PlayerBoxMode : DropdownFeature(
    name = "Player Box Mode",
    description = "",
    category = "General",
    configKey = "player_box_mode",
    subcategory = "Boxes",
    options = listOf("All Players", "Specific Players"),
    dependsOn = BoxPlayers
)

object BoxSpecificPlayer : TextFeature(
    name = "Box Players",
    description = "",
    category = "General",
    configKey = "box_specific_player",
    subcategory = "Boxes",
    dependsOn = PlayerBoxMode,
    placeholder = "IGN1, IGN2, ..."
) {
    init {
        showWhen { PlayerBoxMode.selectedOption == "Specific Players" }
    }
    fun matches(playerName: String): Boolean =
        value.split(',')
            .any { it.trim().equals(playerName, ignoreCase = true) && it.isNotBlank() }
}
