package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P2State

object PadHelper : SwitchFeature(
    name = "Pad Helper (PY)",
    description = "Shows how far to pad §5purple §rand §eyellow §rto hit Storm.\nDoes §lnot §rhelp with pre-crushing.",
    category = "Dungeons",
    configKey = "pad_helper",
    subcategory = "P2",
) {
    private const val PURPLE = 0xAA00FF
    private const val YELLOW = 0xFFFF00
    private const val FILL_ALPHA = 0x40 shl 24      // ~25%
    private const val LINE_ALPHA = 0xFF shl 24
    private const val LINE_WIDTH = 4f

    private val OUTLINE = listOf(
        -3 to -1, -3 to 2, -2 to 2, -2 to 3, -1 to 3, -1 to 4,
        2 to 4, 2 to 3, 3 to 3, 3 to 2, 4 to 2, 4 to -1,
        3 to -1, 3 to -2, 2 to -2, 2 to -3, -1 to -3, -1 to -2,
        -2 to -2, -2 to -1,
    )

    private val FILL = listOf(
        intArrayOf(-3, -1, -2, 2),
        intArrayOf(-2, -2, -1, 3),
        intArrayOf(-1, -3, 0, 4),
        intArrayOf(0, -3, 1, 4),
        intArrayOf(1, -3, 2, 4),
        intArrayOf(2, -2, 3, 3),
        intArrayOf(3, -1, 4, 2),
    )

    private data class Pad(val cx: Int, val cz: Int, val topY: Int, val rgb: Int)

    private val pads = listOf(
        Pad(cx = 46, cz = 65, topY = 176, rgb = YELLOW),    // blocks at y=175
        Pad(cx = 100, cz = 65, topY = 181, rgb = PURPLE),   // blocks at y=180
    )

    init {
        onRender { ctx ->
            if (!DungeonState.inDungeon || !P2State.inP2) return@onRender
            for (p in pads) {
                val y = p.topY.toDouble()
                val rects = FILL.map {
                    doubleArrayOf(
                        (p.cx + it[0]).toDouble(), (p.cz + it[1]).toDouble(),
                        (p.cx + it[2]).toDouble(), (p.cz + it[3]).toDouble(),
                    )
                }
                ESP.drawWorldFlatFill(ctx, y, rects, FILL_ALPHA or p.rgb, depth = true)
                val outline = OUTLINE.map {
                    (p.cx + it.first).toDouble() to (p.cz + it.second).toDouble()
                }
                ESP.drawWorldFlatLoop(ctx, y, outline, LINE_ALPHA or p.rgb, depth = true, lineWidth = LINE_WIDTH)
            }
        }
    }
}
