package io.github.meridian.features.impl.general

import io.github.meridian.features.SwitchFeature

object IRLTime : SwitchFeature(
    name = "IRL Time",
    description = "Displays your computer's real-world clock as a movable HUD element.\nEdit its position with /md hud.",
    category = "General",
    configKey = "irl_time",
    subcategory = "Miscellaneous",
)