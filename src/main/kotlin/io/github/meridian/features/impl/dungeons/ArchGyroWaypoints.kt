package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP


object ArchGyroWaypoint : SwitchFeature(
    name = "Archer Gyro Waypoint",
    description = "Highlights the block to gyro in P1 for archer High Crit",
    category = "Dungeons",
    configKey = "archer_gyro_waypoints",
    subcategory = "P1"
) {
    private const val X0 = 76.0; private const val Y0 = 220.0; private const val Z0 = 36.0
    private const val X1 = 77.0; private const val Y1 = 221.0; private const val Z1 = 37.0

    private const val COLOR = 0xFF00FF00.toInt()

    @Volatile private var active = false

    init {
        onChat { text, _, _ ->
            if (text.startsWith("[BOSS] Maxor: WELL WELL WELL LOOK WHO'S HERE!")) active = true
            else if (text.startsWith("[BOSS] Maxor: I'M TOO YOUNG TO DIE AGAIN!")) active = false
        }
        onRender { ctx ->
            if (!active) return@onRender
            ESP.drawWorldBox(ctx, X0, Y0, Z0, X1, Y1, Z1, COLOR, depth = true)
        }
    }
}