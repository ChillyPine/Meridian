package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.DropdownFeature
import io.github.meridian.features.SwitchFeature
import io.github.meridian.features.TextFeature
import io.github.meridian.utils.ESP
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.player.RemotePlayer

// Only box players within this many blocks of the local player.
private const val PLAYER_ESP_RADIUS = 75.0

object PlayerESP : SwitchFeature(
    name = "Player ESP",
    description = "",
    category = "General",
    configKey = "player_esp",
    subcategory = "ESPs"
) {
    init {
        WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
            if (!enabled) return@register
            val level = Meridian.mc.level ?: return@register
            val self = Meridian.mc.player ?: return@register

            for (ent in level.entitiesForRendering()) {
                // RemotePlayer is every *other* player; the local player is a
                // LocalPlayer, so self is excluded for free.
                if (ent !is RemotePlayer) continue
                if (ent.distanceToSqr(self.x, self.y, self.z) > PLAYER_ESP_RADIUS * PLAYER_ESP_RADIUS) continue

                when (PlayerESPMode.selectedOption) {
                    "All Players" -> {}
                    "Specific Players" -> if (!SpecificPlayerESP.matches(ent.gameProfile.name)) continue
                    else -> continue
                }

                // depth defaults to ESP.depth, so this honors /md depth automatically.
                ESP.drawBox(ctx, ent, w = 1.0, h = 2.0, wz = 0.6, argb = 0xFF00B300.toInt())
            }
        }
    }
}

object PlayerESPMode : DropdownFeature(
    name = "Player ESP Mode",
    description = "",
    category = "General",
    configKey = "player_esp_mode",
    subcategory = "ESPs",
    options = listOf("All Players", "Specific Players"),
    dependsOn = PlayerESP
)

object SpecificPlayerESP : TextFeature(
    name = "Player ESP",
    description = "",
    category = "General",
    configKey = "specific_player_esp",
    subcategory = "ESPs",
    dependsOn = PlayerESPMode,
    placeholder = "IGN1, IGN2, ..."
) {
    init {
        showWhen { PlayerESPMode.selectedOption == "Specific Players" }
    }

    // True if the given IGN is in the comma-separated allowlist (case-insensitive).
    fun matches(playerName: String): Boolean =
        value.split(',')
            .any { it.trim().equals(playerName, ignoreCase = true) && it.isNotBlank() }
}
