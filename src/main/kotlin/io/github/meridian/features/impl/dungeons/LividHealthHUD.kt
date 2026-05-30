package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.SwitchFeature

// Shows the real Livid's health as a HUD element.
object LividHealthHUD : SwitchFeature(
    name = "Livid Health Display",
    description = "Shows Livid's remaining health as a HUD element during the F5/M5 boss.\nEdit its position with /md hud.",
    category = "Dungeons",
    configKey = "livid_health_hud",
    subcategory = "M5",
)