package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.P5State
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents

object StackHelper : SwitchFeature(
    name = "Arrow Stack Helper",
    description = "Shows you where to stand and aim during P5 in M7.",
    category = "Dungeons",
    configKey = "stack_helper",
    subcategory = "P5"
) {
    private data class Circle(val x: Double, val y: Double, val z: Double, val r: Double, val rgb: Int)
    private data class AimBox(val x: Double, val y: Double, val z: Double, val rgb: Int)

    private const val MAGENTA = 0xFF00FF
    private const val BLUE = 0x0000FF
    private const val GREEN = 0x00FF00
    private const val RED = 0xFF0000
    private const val ORANGE = 0xFF9900

    // 0.5 alpha for floor rings, ~0.4 fill for boxes (halved from 0xCC by ESP).
    private const val CIRCLE_ALPHA = 0x80 shl 24
    private const val BOX_ALPHA = 0xCC shl 24

    private val circles = listOf(
        Circle(34.0, 6.0, 102.0, 1.5, MAGENTA),
        Circle(77.0, 6.0, 102.0, 1.5, MAGENTA),
        Circle(45.0, 6.0, 102.0, 1.5, BLUE),
        Circle(58.0, 5.0, 71.0, 1.5, GREEN),
        Circle(31.0, 5.0, 88.0, 1.2, RED),
        Circle(16.0, 5.0, 88.0, 1.2, RED),
        Circle(78.0, 5.0, 92.0, 1.5, ORANGE),
    )

    private val aimBoxes = listOf(
        AimBox(56.0, 21.3, 129.0, MAGENTA),
        AimBox(89.0, 22.0, 96.0, BLUE),
        AimBox(23.0, 23.0, 93.4, GREEN),
        AimBox(32.0, 22.0, 60.0, RED),
        AimBox(81.4, 21.6, 57.0, ORANGE),
    )

    init {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx ->
            if (!enabled || !P5State.inP5) return@register

            for (c in circles) {
                ESP.drawWorldCircle(ctx, c.x, c.y, c.z, c.r, CIRCLE_ALPHA or c.rgb, depth = true, lineWidth = 4f)
            }
            for (b in aimBoxes) {
                ESP.drawWorldFilled(
                    ctx,
                    b.x - 0.5, b.y, b.z - 0.5,
                    b.x + 0.5, b.y + 1.0, b.z + 0.5,
                    BOX_ALPHA or b.rgb,
                    depth = true,
                )
            }
        }
    }
}