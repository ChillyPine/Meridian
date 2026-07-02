package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P5State
import net.minecraft.world.phys.Vec3

object RelicHelper : SwitchFeature(
    name = "Relic Helper",
    description = "Marks the exact spot on each end portal frame to aim at when grabbing relics.",
    category = "Dungeons",
    configKey = "relic_helper",
    subcategory = "P5"
) {
    // The end portal frame hitbox fills its footprint but is only 13/16 tall.
    private const val FRAME_HEIGHT = 13.0 / 16.0

    // Square geometry, in blocks (a face is 16px = 1 block). Tunable:
    //   SIZE  - edge length of the marker square (2px)
    //   INSET - gap between the square and the two face edges it hugs (flush)
    //   EPS   - push off the face toward the player so the outline doesn't z-fight
    private const val SIZE = 2.0 / 16.0
    private const val INSET = 0.0
    private const val EPS = 0.01

    private enum class Face { WEST, NORTH }

    /** Player-perspective horizontal side of the face (facing the block). */
    private enum class Side { LEFT, RIGHT }

    private class Relic(
        val bx: Int, val by: Int, val bz: Int,
        val face: Face,
        val side: Side,
        val color: Int,
    )

    // All five markers sit at the TOP of the face; only the left/right side
    // differs. Sides are from the player's view while facing the block, which
    // is the opposite direction to the given face's outward normal.
    // Colors match Arrow Stack Helper's per-relic colors (0xFF alpha added).
    private val relics = listOf(
        Relic(20, 6, 60, Face.NORTH, Side.LEFT, 0xFFFF0000.toInt()),  // Red
        Relic(20, 6, 95, Face.NORTH, Side.LEFT, 0xFF00FF00.toInt()),  // Green
        Relic(56, 8, 133, Face.NORTH, Side.LEFT, 0xFFFF00FF.toInt()), // Purple (magenta)
        Relic(92, 6, 94, Face.WEST, Side.RIGHT, 0xFF0000FF.toInt()),  // Blue
        Relic(93, 6, 56, Face.WEST, Side.RIGHT, 0xFFFF9900.toInt()),  // Orange
    )

    /** Four corners of the marker square, laid on the relic's block face. */
    private fun corners(r: Relic): List<Vec3> {
        val yTop = r.by + FRAME_HEIGHT - INSET
        val yBot = yTop - SIZE
        return when (r.face) {
            // West face lies in the plane x = bx; player looks +X, so their
            // left is -Z (north) and right is +Z (south).
            Face.WEST -> {
                val x = r.bx - EPS
                val (zA, zB) = when (r.side) {
                    Side.LEFT -> r.bz + INSET to r.bz + INSET + SIZE
                    Side.RIGHT -> r.bz + 1 - INSET - SIZE to r.bz + 1 - INSET
                }
                listOf(
                    Vec3(x, yTop, zA), Vec3(x, yTop, zB),
                    Vec3(x, yBot, zB), Vec3(x, yBot, zA),
                )
            }
            Face.NORTH -> {
                val z = r.bz - EPS
                val (xA, xB) = when (r.side) {
                    Side.LEFT -> r.bx + 1 - INSET - SIZE to r.bx + 1 - INSET
                    Side.RIGHT -> r.bx + INSET to r.bx + INSET + SIZE
                }
                listOf(
                    Vec3(xA, yTop, z), Vec3(xB, yTop, z),
                    Vec3(xB, yBot, z), Vec3(xA, yBot, z),
                )
            }
        }
    }

    private fun halfAlpha(argb: Int): Int {
        val a = ((argb ushr 24) and 0xFF) / 2
        return (a shl 24) or (argb and 0x00FFFFFF)
    }

    init {
        onRender(P5State.state) { ctx ->
            for (r in relics) {
                val pts = corners(r)
                // depth = true is pinned (not the addon-overridable ESP.depth) so
                // the relic markers stay line-of-sight only regardless of Meridian Extras.
                ESP.drawWorldQuadFill(ctx, pts, halfAlpha(r.color), depth = true)
                ESP.drawWorldLineLoop(ctx, pts, r.color, depth = true)
            }
        }
    }
}