package io.github.meridian.gui

import net.minecraft.client.gui.GuiGraphics
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun drawRoundedRect(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int, radius: Int) {
    if (w <= 0 || h <= 0) return
    val r = max(0, min(radius, min(w, h) / 2))
    if (r == 0) {
        ctx.fill(x, y, x + w, y + h, color)
        return
    }

    val alpha = (color ushr 24) and 0xFF
    val rgb = color and 0x00FFFFFF

    // Body in three non-overlapping bands so transparent fills don't double up.
    ctx.fill(x, y + r, x + w, y + h - r, color)              // middle, full width
    ctx.fill(x + r, y, x + w - r, y + r, color)              // top, between corners
    ctx.fill(x + r, y + h - r, x + w - r, y + h, color)      // bottom, between corners

    drawAntialiasedCorner(ctx, x,         y,         r, alpha, rgb, false, false)
    drawAntialiasedCorner(ctx, x + w - r, y,         r, alpha, rgb, true,  false)
    drawAntialiasedCorner(ctx, x,         y + h - r, r, alpha, rgb, false, true)
    drawAntialiasedCorner(ctx, x + w - r, y + h - r, r, alpha, rgb, true,  true)
}

private fun drawAntialiasedCorner(
    ctx: GuiGraphics,
    cornerX: Int,
    cornerY: Int,
    radius: Int,
    alpha: Int,
    rgb: Int,
    isRight: Boolean,
    isBottom: Boolean
) {
    // The circle's center sits at the inside corner of the radius square.
    val centerX = if (isRight) cornerX.toDouble() else (cornerX + radius).toDouble()
    val centerY = if (isBottom) cornerY.toDouble() else (cornerY + radius).toDouble()

    for (py in 0 until radius) {
        for (px in 0 until radius) {
            val dx = (cornerX + px + 0.5) - centerX
            val dy = (cornerY + py + 0.5) - centerY
            val distance = sqrt(dx * dx + dy * dy)
            // Coverage: 1 fully inside the circle, 0 fully outside, fractional at the edge.
            val coverage = (radius - distance + 0.5).coerceIn(0.0, 1.0)
            if (coverage <= 0.0) continue
            val pixelAlpha = (alpha * coverage).toInt() and 0xFF
            val pixelColor = (pixelAlpha shl 24) or rgb
            ctx.fill(cornerX + px, cornerY + py, cornerX + px + 1, cornerY + py + 1, pixelColor)
        }
    }
}
