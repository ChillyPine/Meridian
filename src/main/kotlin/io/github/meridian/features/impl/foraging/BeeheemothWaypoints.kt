package io.github.meridian.features.impl.foraging

import io.github.meridian.features.types.SwitchFeature

object BeeheemothWaypoints : SwitchFeature(
    name = "Beeheemoth Spawn Waypoints",
    description = "",
    category = "General",
    configKey = "beeheemoth_waypoints",
    subcategory = "Torrhus Canyon",
) {
    init {
        /*
        BEEHEEMOTH! A Beeheemoth has spawned at Critter Safari Entrance!
        BEEHEEMOTH! A Beeheemoth has spawned at Torrhus Springs!
        BEEHEEMOTH! A Beeheemoth has spawned at Mega Tree!
        Safari: -711 108 188
        Springs: -640 163 164
        Tree: -524 112 289
        BEEHEEMOTH DOWN!
         */
    }
}

object BeeheemothQuickWarp : SwitchFeature(
    name = "Beeheemoth Spawn Waypoints",
    description = "",
    category = "General",
    configKey = "box_pangolins",
    subcategory = "Torrhus Canyon",
    dependsOn = BeeheemothWaypoints
) {
    init {

    }
}