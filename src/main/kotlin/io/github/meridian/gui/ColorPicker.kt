package io.github.meridian.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import io.github.meridian.utils.playClickSound
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent

class ColorPicker : Screen(Component.literal("Meridian")) {

    companion object {
        private const val PANEL_WIDTH = 400
        private const val PANEL_HEIGHT = 280

        private const val PANEL_COLOR = 0x1E1E22
        private const val PANEL_OPACITY = 200

        private const val TITLE_TEXT = "Color Picker"
        private const val TITLE_COLOR = 0xFFFFFFFF.toInt()
        private const val TITLE_TOP_PADDING = 8

        private const val BUTTON_HEIGHT = 14
        private const val BUTTON_INNER_PADDING_X = 8
        private const val BUTTON_BOTTOM_PADDING = 10
        private const val BUTTON_TEXT_COLOR = 0xFFFFFFFF.toInt()
        private const val CONFIRM_LABEL = "Confirm"
        private const val BACK_LABEL = "Back"
        private const val CONFIRM_COLOR = 0xFF4CAF50.toInt()
        private const val BACK_COLOR = 0xFF666666.toInt()

        // SV picker
        private const val SV_TOP_PADDING = 22
        private const val SV_HEIGHT = 100
        private const val SV_SIDE_PADDING = 12

        // Sliders
        private const val SLIDER_HEIGHT = 8
        private const val SLIDER_TOP_GAP = 8
        private const val THUMB_RADIUS = 5

        // Hex input area
        private const val HEX_TOP_GAP = 8
        private const val HEX_HEIGHT = 14
        private const val PREVIEW_SIZE = 14

        var lastConfirmedColor: Int = 0xFF_BB86FC.toInt() // default purple

    }

    // HSV + alpha state (hue 0-360, sat/val 0-1, alpha 0-255)
    private var hue = 0f
    private var sat = 1f
    private var value = 1f
    private var alpha = 255

    // Hex input
    private var hexInput = "FFFFFFFF"
    private var hexCursorPos = 8
    private var hexFocused = false

    // Dragging state
    private enum class Drag { NONE, SV, HUE, ALPHA }
    private var dragging = Drag.NONE

    // Cached layout rects (set in render, used for hit-testing)
    private var svX = 0; private var svY = 0; private var svW = 0
    private var hueX = 0; private var hueY = 0; private var hueW = 0
    private var alphaX = 0; private var alphaY = 0; private var alphaW = 0
    private var hexX = 0; private var hexY = 0; private var hexW = 0
    private var confirmX = 0; private var confirmY = 0; private var confirmW = 0
    private var backX = 0;   private var backY = 0;   private var backW = 0

    // Callback invoked with ARGB int when user confirms
    var onConfirm: ((Int) -> Unit)? = null


    override fun renderBackground(g: GuiGraphics, mx: Int, my: Int, pt: Float) {}

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val px = (width - PANEL_WIDTH) / 2
        val py = (height - PANEL_HEIGHT) / 2
        val panelColor = (PANEL_OPACITY shl 24) or PANEL_COLOR

        g.fill(px, py, px + PANEL_WIDTH, py + PANEL_HEIGHT, panelColor)

        val titleX = px + (PANEL_WIDTH - font.width(TITLE_TEXT)) / 2
        g.drawString(font, TITLE_TEXT, titleX, py + TITLE_TOP_PADDING, TITLE_COLOR, false)

        svX = px + SV_SIDE_PADDING
        svY = py + SV_TOP_PADDING + font.lineHeight
        svW = PANEL_WIDTH - SV_SIDE_PADDING * 2

        renderSVPicker(g, svX, svY, svW, SV_HEIGHT)

        val sliderY1 = svY + SV_HEIGHT + SLIDER_TOP_GAP
        val sliderY2 = sliderY1 + SLIDER_HEIGHT + SLIDER_TOP_GAP + 4
        val sliderW = svW - 24
        val sliderXStart = svX + 18

        hueX = sliderXStart; hueY = sliderY1; hueW = sliderW
        alphaX = sliderXStart; alphaY = sliderY2; alphaW = sliderW

        renderHueSlider(g, hueX, hueY, hueW, SLIDER_HEIGHT)
        renderAlphaSlider(g, alphaX, alphaY, alphaW, SLIDER_HEIGHT)

        // Slider labels
        g.drawString(font, "H", svX, hueY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFAAAAAA.toInt(), false)
        g.drawString(font, "A", svX, alphaY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFAAAAAA.toInt(), false)

        // Slider values
        val hueVal = hue.toInt().toString()
        val alphaVal = "${(alpha * 100 / 255)}%"
        val valX = sliderXStart + sliderW + 4
        g.drawString(font, hueVal, valX, hueY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFCCCCCC.toInt(), false)
        g.drawString(font, alphaVal, valX, alphaY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFCCCCCC.toInt(), false)

        // Hex input row
        hexY = sliderY2 + SLIDER_HEIGHT + HEX_TOP_GAP + 4
        hexX = svX + font.width("Hex ") + 4
        hexW = svW - font.width("Hex ") - PREVIEW_SIZE - 8 - 4
        hexX = svX + 28
        hexW = svW - 28 - PREVIEW_SIZE - 8

        renderHexInput(g, px, hexX, hexY, hexW)

        // Color preview swatch
        val previewX = hexX + hexW + 4
        g.fill(previewX, hexY, previewX + PREVIEW_SIZE, hexY + HEX_HEIGHT, currentArgb())

        // Buttons
        val buttonY = py + PANEL_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM_PADDING
        backW = font.width(BACK_LABEL) + 2 * BUTTON_INNER_PADDING_X
        backX = px + 10; backY = buttonY
        renderButton(g, backX, backY, backW, BACK_LABEL, BACK_COLOR)

        confirmW = font.width(CONFIRM_LABEL) + 2 * BUTTON_INNER_PADDING_X
        confirmX = px + PANEL_WIDTH - confirmW - 10; confirmY = buttonY
        renderButton(g, confirmX, confirmY, confirmW, CONFIRM_LABEL, CONFIRM_COLOR)

        super.render(g, mouseX, mouseY, partialTick)
    }

    private fun renderSVPicker(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        val hueColor = hsvToRgb(hue, 1f, 1f) or (0xFF shl 24)
        val strip = 2
        var col = 0
        while (col < w) {
            val s = col.toFloat() / w
            val r0 = lerp(255, (hueColor shr 16) and 0xFF, s)
            val g0 = lerp(255, (hueColor shr 8)  and 0xFF, s)
            val b0 = lerp(255,  hueColor          and 0xFF, s)
            var row = 0
            while (row < h) {
                val v = 1f - row.toFloat() / h
                val r  = (r0 * v).toInt()
                val gv = (g0 * v).toInt()
                val b  = (b0 * v).toInt()
                g.fill(x + col, y + row, x + col + strip, y + row + strip,
                    (0xFF shl 24) or (r shl 16) or (gv shl 8) or b)
                row += strip
            }
            col += strip
        }
        val cx = x + (sat * w).toInt()
        val cy = y + ((1f - value) * h).toInt()
        g.fill(cx - 4, cy - 1, cx + 4, cy + 1, 0xFFFFFFFF.toInt())
        g.fill(cx - 1, cy - 4, cx + 1, cy + 4, 0xFFFFFFFF.toInt())
    }

    private fun renderHueSlider(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        // Hue gradient (6 stops)
        val stops = intArrayOf(0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF, 0xFF0000)
        val segW = w / 6
        for (i in 0 until 6) {
            val c1 = stops[i] or (0xFF shl 24)
            val c2 = stops[i + 1] or (0xFF shl 24)
            for (col in 0 until segW) {
                val t = col.toFloat() / segW
                val color = lerpColor(c1, c2, t)
                g.fill(x + i * segW + col, y, x + i * segW + col + 1, y + h, color)
            }
        }
        // Thumb
        val tx = x + (hue / 360f * w).toInt()
        g.fill(tx - THUMB_RADIUS, y - 2, tx + THUMB_RADIUS, y + h + 2, 0xFFFFFFFF.toInt())
        g.fill(tx - THUMB_RADIUS + 1, y - 1, tx + THUMB_RADIUS - 1, y + h + 1, 0xFF333333.toInt())
    }

    private fun renderAlphaSlider(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        // Checkerboard
        val cellSize = 4
        for (col in 0 until w step cellSize) {
            for (row in 0 until h step cellSize) {
                val light = ((col / cellSize + row / cellSize) % 2 == 0)
                g.fill(x + col, y + row, x + col + cellSize, y + row + cellSize,
                    if (light) 0xFFCCCCCC.toInt() else 0xFF999999.toInt())
            }
        }
        // Gradient overlay: transparent→current color at full alpha
        val rgb = hsvToRgb(hue, sat, value)
        for (col in 0 until w) {
            val a = (col.toFloat() / w * 255).toInt()
            val color = (a shl 24) or rgb
            g.fill(x + col, y, x + col + 1, y + h, color)
        }
        // Thumb
        val tx = x + (alpha.toFloat() / 255f * w).toInt()
        g.fill(tx - THUMB_RADIUS, y - 2, tx + THUMB_RADIUS, y + h + 2, 0xFFFFFFFF.toInt())
        g.fill(tx - THUMB_RADIUS + 1, y - 1, tx + THUMB_RADIUS - 1, y + h + 1, 0xFF333333.toInt())
    }

    private fun renderHexInput(g: GuiGraphics, panelX: Int, x: Int, y: Int, w: Int) {
        g.drawString(font, "Hex", panelX + SV_SIDE_PADDING, y + (HEX_HEIGHT - font.lineHeight) / 2 + 1, 0xFFAAAAAA.toInt(), false)
        val bgColor = if (hexFocused) 0xFF2A2A38.toInt() else 0xFF222228.toInt()
        g.fill(x, y, x + w, y + HEX_HEIGHT, bgColor)
        g.fill(x, y, x + w, y + 1, if (hexFocused) 0xFFBB86FC.toInt() else 0xFF444444.toInt())
        g.fill(x, y + HEX_HEIGHT - 1, x + w, y + HEX_HEIGHT, if (hexFocused) 0xFFBB86FC.toInt() else 0xFF444444.toInt())
        g.fill(x, y, x + 1, y + HEX_HEIGHT, if (hexFocused) 0xFFBB86FC.toInt() else 0xFF444444.toInt())
        g.fill(x + w - 1, y, x + w, y + HEX_HEIGHT, if (hexFocused) 0xFFBB86FC.toInt() else 0xFF444444.toInt())
        val display = "#$hexInput"
        val textY = y + (HEX_HEIGHT - font.lineHeight) / 2 + 1
        g.drawString(font, display, x + 3, textY, 0xFFFFFFFF.toInt(), false)
        if (hexFocused && (System.currentTimeMillis() / 500) % 2 == 0L) {
            val cursorX = x + 3 + font.width("#" + hexInput.substring(0, hexCursorPos))
            g.fill(cursorX, y + 2, cursorX + 1, y + HEX_HEIGHT - 2, 0xFFFFFFFF.toInt())
        }
    }

    private fun renderButton(g: GuiGraphics, bx: Int, by: Int, bw: Int, label: String, bgColor: Int) {
        g.fill(bx, by, bx + bw, by + BUTTON_HEIGHT, bgColor)
        val lx = bx + (bw - font.width(label)) / 2
        val ly = by + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1
        g.drawString(font, label, lx, ly, BUTTON_TEXT_COLOR, false)
    }


    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = event.x.toInt(); val my = event.y.toInt()

        hexFocused = mx in hexX until (hexX + hexW) && my in hexY until (hexY + HEX_HEIGHT)

        if (mx in svX until (svX + svW) && my in svY until (svY + SV_HEIGHT)) {
            dragging = Drag.SV; updateSV(mx, my); return true
        }
        if (mx in (hueX - THUMB_RADIUS)..(hueX + hueW + THUMB_RADIUS) && my in (hueY - 4)..(hueY + SLIDER_HEIGHT + 4)) {
            dragging = Drag.HUE; updateHue(mx); return true
        }
        if (mx in (alphaX - THUMB_RADIUS)..(alphaX + alphaW + THUMB_RADIUS) && my in (alphaY - 4)..(alphaY + SLIDER_HEIGHT + 4)) {
            dragging = Drag.ALPHA; updateAlpha(mx); return true
        }
        if (mx in backX until (backX + backW) && my in backY until (backY + BUTTON_HEIGHT)) {
            playClickSound(); Minecraft.getInstance().setScreen(MeridianScreen()); return true
        }
        if (mx in confirmX until (confirmX + confirmW) && my in confirmY until (confirmY + BUTTON_HEIGHT)) {
            onConfirm?.invoke(currentArgb())
            lastConfirmedColor = currentArgb() // always save it
            playClickSound()
            Minecraft.getInstance().setScreen(MeridianScreen())
            return true
        }
        return super.mouseClicked(event, bl)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        val mx = event.x.toInt(); val my = event.y.toInt()
        when (dragging) {
            Drag.SV    -> updateSV(mx, my)
            Drag.HUE   -> updateHue(mx)
            Drag.ALPHA -> updateAlpha(mx)
            Drag.NONE  -> {}
        }
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        dragging = Drag.NONE
        return super.mouseReleased(event)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (!hexFocused) return super.keyPressed(event)
        when (event.key) {
            259 -> { // Backspace
                if (hexCursorPos > 0) {
                    hexInput = hexInput.removeRange(hexCursorPos - 1, hexCursorPos)
                    hexCursorPos--
                    applyHexInput()
                }
            }
            261 -> { // Delete
                if (hexCursorPos < hexInput.length) {
                    hexInput = hexInput.removeRange(hexCursorPos, hexCursorPos + 1)
                    applyHexInput()
                }
            }
            263 -> if (hexCursorPos > 0) hexCursorPos--
            262 -> if (hexCursorPos < hexInput.length) hexCursorPos++
            268 -> hexCursorPos = 0
            269 -> hexCursorPos = hexInput.length
            else -> return super.keyPressed(event)
        }
        return true
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (!hexFocused) return false
        if (hexInput.length >= 8) return false
        val c = Character.toChars(event.codepoint).first().uppercaseChar()
        if (c in '0'..'9' || c in 'A'..'F') {
            hexInput = hexInput.substring(0, hexCursorPos) + c + hexInput.substring(hexCursorPos)
            hexCursorPos++
            applyHexInput()
            return true
        }
        return false
    }

    private fun updateSV(mx: Int, my: Int) {
        sat   = ((mx - svX).toFloat() / svW).coerceIn(0f, 1f)
        value = 1f - ((my - svY).toFloat() / SV_HEIGHT).coerceIn(0f, 1f)
        syncHexFromHsv()
    }

    private fun updateHue(mx: Int) {
        hue = ((mx - hueX).toFloat() / hueW * 360f).coerceIn(0f, 360f)
        syncHexFromHsv()
    }

    private fun updateAlpha(mx: Int) {
        alpha = ((mx - alphaX).toFloat() / alphaW * 255f).coerceIn(0f, 255f).toInt()
        syncHexFromHsv()
    }

    private fun syncHexFromHsv() {
        val rgb = hsvToRgb(hue, sat, value)
        val r = (rgb shr 16) and 0xFF
        val gv = (rgb shr 8) and 0xFF
        val b = rgb and 0xFF
        hexInput = "%02X%02X%02X%02X".format(alpha, r, gv, b)
        hexCursorPos = hexCursorPos.coerceIn(0, hexInput.length)
    }

    private fun applyHexInput() {
        val padded = hexInput.padEnd(8, '0')
        try {
            val v = java.lang.Long.parseLong(padded, 16)
            alpha = ((v shr 24) and 0xFF).toInt()
            val r = ((v shr 16) and 0xFF).toInt()
            val gv = ((v shr 8) and 0xFF).toInt()
            val b = (v and 0xFF).toInt()
            val hsv = rgbToHsv(r, gv, b)
            hue = hsv[0]; sat = hsv[1]; value = hsv[2]
        } catch (_: NumberFormatException) {}
    }

    private fun currentArgb(): Int {
        val rgb = hsvToRgb(hue, sat, value)
        return (alpha shl 24) or rgb
    }

    private fun hsvToRgb(h: Float, s: Float, v: Float): Int {
        val hh = h / 60f
        val i = hh.toInt()
        val ff = hh - i
        val p = v * (1f - s)
        val q = v * (1f - s * ff)
        val t = v * (1f - s * (1f - ff))
        val (r, g, b) = when (i % 6) {
            0 -> Triple(v, t, p); 1 -> Triple(q, v, p)
            2 -> Triple(p, v, t); 3 -> Triple(p, q, v)
            4 -> Triple(t, p, v); else -> Triple(v, p, q)
        }
        return ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
    }

    private fun rgbToHsv(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f; val gf = g / 255f; val bf = b / 255f
        val max = maxOf(rf, gf, bf); val min = minOf(rf, gf, bf)
        val delta = max - min
        val h = when {
            delta == 0f -> 0f
            max == rf   -> 60f * (((gf - bf) / delta) % 6)
            max == gf   -> 60f * ((bf - rf) / delta + 2)
            else        -> 60f * ((rf - gf) / delta + 4)
        }.let { if (it < 0) it + 360f else it }
        val s = if (max == 0f) 0f else delta / max
        return floatArrayOf(h, s, max)
    }

    private fun lerp(a: Int, b: Int, t: Float) = (a + (b - a) * t).toInt()
    private fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val a = lerp((c1 shr 24) and 0xFF, (c2 shr 24) and 0xFF, t)
        val r = lerp((c1 shr 16) and 0xFF, (c2 shr 16) and 0xFF, t)
        val g = lerp((c1 shr 8)  and 0xFF, (c2 shr 8)  and 0xFF, t)
        val b = lerp(c1 and 0xFF, c2 and 0xFF, t)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    override fun isPauseScreen(): Boolean = false
}