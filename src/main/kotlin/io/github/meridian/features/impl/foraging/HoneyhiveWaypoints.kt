package io.github.meridian.features.impl.foraging

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.Island
import io.github.meridian.utils.SkyblockLocation
import net.minecraft.core.BlockPos

object HoneyhiveWaypoints : SwitchFeature(
    name = "Honeyhive Waypoints",
    description = "Adds waypoints for the honeyhives (beehives) in Torrhus Canyon.",
    category = "Foraging",
    configKey = "honeyhive_waypoints",
    subcategory = "Torrhus Canyon"
) {
    private const val COLOR = 0xFFFFFF00.toInt() // opaque yellow

    private val HIVES = listOf(
        BlockPos(-693, 93, 153),
        BlockPos(-696, 93, 150),
        BlockPos(-693, 94, 147),
        BlockPos(-611, 98, 274),
        BlockPos(-606, 98, 275),
        BlockPos(-588, 150, 257),
        BlockPos(-581, 152, 258),
        BlockPos(-578, 151, 256),
        BlockPos(-724, 93, 211),
        BlockPos(-721, 92, 207),
        BlockPos(-724, 92, 204),
        BlockPos(-572, 101, 206),
        BlockPos(-577, 102, 205),
        BlockPos(-665, 97, 167),
        BlockPos(-665, 96, 170),
    )

    init {
        onRender(SkyblockLocation.on(Island.TORRHUS_CANYON)) { ctx ->
            for (p in HIVES) {
                ESP.drawWorldBox(
                    ctx,
                    p.x.toDouble(), p.y.toDouble(), p.z.toDouble(),
                    p.x + 1.0, p.y + 1.0, p.z + 1.0,
                    COLOR,
                )
            }
        }
    }
}
