package io.github.meridian.gui

import com.mojang.blaze3d.platform.NativeImage
import io.github.meridian.utils.playClickSound
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class ColorPicker(
    initialArgb: Int,
    private val parent: Screen?,
    private val onConfirm: (Int) -> Unit
) : Screen(Component.literal("Meridian")) {

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

        private const val SV_TOP_PADDING = 22
        private const val SV_HEIGHT = 100
        private const val SV_SIDE_PADDING = 12

        private const val SLIDER_HEIGHT = 8
        private const val SLIDER_TOP_GAP = 8
        private const val THUMB_RADIUS = 5

        private const val HEX_TOP_GAP = 8
        private const val HEX_HEIGHT = 14
        private const val PREVIEW_SIZE = 14

        private const val TEX_NAMESPACE = "meridian"

        private var instanceCounter = 0

        private fun argbToAbgr(argb: Int): Int {
            val a = (argb ushr 24) and 0xFF
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            return (a shl 24) or (b shl 16) or (g shl 8) or r
        }
    }

    private var hue = 0f
    private var sat = 1f
    private var value = 1f
    private var alpha = 255

    private var hexInput = "FFFFFFFF"
    private var hexCursorPos = 8
    private var hexFocused = false

    private enum class Drag { NONE, SV, HUE, ALPHA }
    private var dragging = Drag.NONE

    private var svX = 0; private var svY = 0; private var svW = 0
    private var hueX = 0; private var hueY = 0; private var hueW = 0
    private var alphaX = 0; private var alphaY = 0; private var alphaW = 0
    private var hexX = 0; private var hexY = 0; private var hexW = 0
    private var confirmX = 0; private var confirmY = 0; private var confirmW = 0
    private var backX = 0;   private var backY = 0;   private var backW = 0

    private val instanceId = instanceCounter++
    private val svTexId = Identifier.fromNamespaceAndPath(TEX_NAMESPACE, "colorpicker/sv_$instanceId")
    private val hueTexId = Identifier.fromNamespaceAndPath(TEX_NAMESPACE, "colorpicker/hue_$instanceId")
    private val alphaTexId = Identifier.fromNamespaceAndPath(TEX_NAMESPACE, "colorpicker/alpha_$instanceId")

    private var svTex: DynamicTexture? = null
    private var hueTex: DynamicTexture? = null
    private var alphaTex: DynamicTexture? = null

    private var svTexHue = -1f
    private var svTexW = 0; private var svTexH = 0
    private var hueTexW = 0
    private var alphaTexW = 0
    private var alphaTexRgb = -1

    init {
        val a = (initialArgb ushr 24) and 0xFF
        val r = (initialArgb shr 16) and 0xFF
        val g = (initialArgb shr 8) and 0xFF
        val b = initialArgb and 0xFF
        val hsv = rgbToHsv(r, g, b)
        hue = hsv[0]; sat = hsv[1]; value = hsv[2]
        alpha = a
        hexInput = "%02X%02X%02X%02X".format(a, r, g, b)
    }

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

        g.drawString(font, "H", svX, hueY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFAAAAAA.toInt(), false)
        g.drawString(font, "A", svX, alphaY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFAAAAAA.toInt(), false)

        val hueVal = hue.toInt().toString()
        val alphaVal = "${(alpha * 100 / 255)}%"
        val valX = sliderXStart + sliderW + 4
        g.drawString(font, hueVal, valX, hueY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFCCCCCC.toInt(), false)
        g.drawString(font, alphaVal, valX, alphaY + (SLIDER_HEIGHT - font.lineHeight) / 2 + 1, 0xFFCCCCCC.toInt(), false)

        hexY = sliderY2 + SLIDER_HEIGHT + HEX_TOP_GAP + 4
        hexX = svX + 28
        hexW = svW - 28 - PREVIEW_SIZE - 8

        renderHexInput(g, px, hexX, hexY, hexW)

        val previewX = hexX + hexW + 4
        g.fill(previewX, hexY, previewX + PREVIEW_SIZE, hexY + HEX_HEIGHT, currentArgb())

        val buttonY = py + PANEL_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM_PADDING
        backW = font.width(BACK_LABEL) + 2 * BUTTON_INNER_PADDING_X
        backX = px + 10; backY = buttonY
        renderButton(g, backX, backY, backW, BACK_LABEL, BACK_COLOR)

        confirmW = font.width(CONFIRM_LABEL) + 2 * BUTTON_INNER_PADDING_X
        confirmX = px + PANEL_WIDTH - confirmW - 10; confirmY = buttonY
        renderButton(g, confirmX, confirmY, confirmW, CONFIRM_LABEL, CONFIRM_COLOR)

        super.render(g, mouseX, mouseY, partialTick)
    }

    private fun ensureSvTexture(w: Int, h: Int) {
        if (svTex == null || svTexW != w || svTexH != h) {
            svTex?.close()
            val img = NativeImage(w, h, false)
            svTex = DynamicTexture({ "meridian sv picker" }, img)
            Minecraft.getInstance().textureManager.register(svTexId, svTex!!)
            svTexW = w; svTexH = h
            svTexHue = -1f
        }
        if (svTexHue != hue) {
            val tex = svTex!!
            val img = tex.pixels!!
            val hueRgb = hsvToRgb(hue, 1f, 1f)
            val hr = (hueRgb shr 16) and 0xFF
            val hg = (hueRgb shr 8) and 0xFF
            val hb = hueRgb and 0xFF
            for (y in 0 until h) {
                val v = 1f - y.toFloat() / (h - 1)
                for (x in 0 until w) {
                    val s = x.toFloat() / (w - 1)
                    val r = ((255 + (hr - 255) * s) * v).toInt()
                    val gr = ((255 + (hg - 255) * s) * v).toInt()
                    val b = ((255 + (hb - 255) * s) * v).toInt()
                    val abgr = (0xFF shl 24) or (b shl 16) or (gr shl 8) or r
                    img.setPixelABGR(x, y, abgr)
                }
            }
            tex.upload()
            svTexHue = hue
        }
    }

    private fun ensureHueTexture(w: Int, h: Int) {
        if (hueTex != null && hueTexW == w) return
        hueTex?.close()
        val img = NativeImage(w, h, false)
        for (x in 0 until w) {
            val h360 = x.toFloat() / (w - 1) * 360f
            val rgb = hsvToRgb(h360, 1f, 1f)
            val abgr = argbToAbgr((0xFF shl 24) or rgb)
            for (y in 0 until h) img.setPixelABGR(x, y, abgr)
        }
        hueTex = DynamicTexture({ "meridian hue strip" }, img)
        Minecraft.getInstance().textureManager.register(hueTexId, hueTex!!)
        hueTex!!.upload()
        hueTexW = w
    }

    private fun ensureAlphaTexture(w: Int, h: Int) {
        val rgb = hsvToRgb(hue, sat, value)
        if (alphaTex != null && alphaTexW == w && alphaTexRgb == rgb) return
        if (alphaTex == null || alphaTexW != w) {
            alphaTex?.close()
            val img = NativeImage(w, h, false)
            alphaTex = DynamicTexture({ "meridian alpha strip" }, img)
            Minecraft.getInstance().textureManager.register(alphaTexId, alphaTex!!)
            alphaTexW = w
        }
        val tex = alphaTex!!
        val img = tex.pixels!!
        for (x in 0 until w) {
            val a = (x.toFloat() / (w - 1) * 255).toInt()
            val abgr = argbToAbgr((a shl 24) or rgb)
            for (y in 0 until h) img.setPixelABGR(x, y, abgr)
        }
        tex.upload()
        alphaTexRgb = rgb
    }

    private fun renderSVPicker(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        ensureSvTexture(w, h)
        g.blit(RenderPipelines.GUI_TEXTURED, svTexId, x, y, 0f, 0f, w, h, w, h)
        val cx = x + (sat * w).toInt()
        val cy = y + ((1f - value) * h).toInt()
        g.fill(cx - 4, cy - 1, cx + 4, cy + 1, 0xFFFFFFFF.toInt())
        g.fill(cx - 1, cy - 4, cx + 1, cy + 4, 0xFFFFFFFF.toInt())
    }

    private fun renderHueSlider(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        ensureHueTexture(w, h)
        g.blit(RenderPipelines.GUI_TEXTURED, hueTexId, x, y, 0f, 0f, w, h, w, h)
        val tx = x + (hue / 360f * w).toInt()
        g.fill(tx - THUMB_RADIUS, y - 2, tx + THUMB_RADIUS, y + h + 2, 0xFFFFFFFF.toInt())
        g.fill(tx - THUMB_RADIUS + 1, y - 1, tx + THUMB_RADIUS - 1, y + h + 1, 0xFF333333.toInt())
    }

    private fun renderAlphaSlider(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        val cellSize = 4
        for (col in 0 until w step cellSize) {
            for (row in 0 until h step cellSize) {
                val light = ((col / cellSize + row / cellSize) % 2 == 0)
                g.fill(x + col, y + row,
                    minOf(x + col + cellSize, x + w),
                    minOf(y + row + cellSize, y + h),
                    if (light) 0xFFCCCCCC.toInt() else 0xFF999999.toInt())
            }
        }
        ensureAlphaTexture(w, h)
        g.blit(RenderPipelines.GUI_TEXTURED, alphaTexId, x, y, 0f, 0f, w, h, w, h)
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
            playClickSound()
            Minecraft.getInstance().setScreen(parent)
            return true
        }
        if (mx in confirmX until (confirmX + confirmW) && my in confirmY until (confirmY + BUTTON_HEIGHT)) {
            onConfirm(currentArgb())
            playClickSound()
            Minecraft.getInstance().setScreen(parent)
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
            259 -> {
                if (hexCursorPos > 0) {
                    hexInput = hexInput.removeRange(hexCursorPos - 1, hexCursorPos)
                    hexCursorPos--
                    applyHexInput()
                }
            }
            261 -> {
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

    override fun removed() {
        super.removed()
        val tm = Minecraft.getInstance().textureManager
        tm.release(svTexId)
        tm.release(hueTexId)
        tm.release(alphaTexId)
        svTex = null; hueTex = null; alphaTex = null
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

    override fun isPauseScreen(): Boolean = false
}