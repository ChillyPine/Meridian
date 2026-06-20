package io.github.meridian.gui

import io.github.meridian.features.FeatureManager
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

// Full-screen editor opened with `/md hud`. Every registered HUD element is
// drawn with its preview content (so even disabled / data-less elements can be
// placed). Drag to move, scroll to scale, hover to see the owning feature's
// name, R to reset the hovered element. Changes persist on release / scale /
// close.
class HudEditScreen : Screen(Component.literal("Meridian HUD Editor")) {

    private var dragging: HudElement? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    private var hovered: HudElement? = null
    private var hoverStartMs = 0L

    override fun extractBackground(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Dim the world so the HUD elements stand out while editing.
        guiGraphics.fill(0, 0, width, height, BG_DIM)
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Screen.render() invokes our renderBackground() (the dim) first.
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick)

        // Header / instructions.
        val title = "§lHUD Editor"
        guiGraphics.text(font, title, (width - font.width(title)) / 2, 6, ACCENT_COLOR, true)
        val help = "Drag to move  ·  Scroll to scale  ·  R resets hovered  ·  Esc saves & exits"
        guiGraphics.text(font, help, (width - font.width(help)) / 2, 6 + font.lineHeight + 2, HELP_COLOR, true)

        // Draw each element's preview (also refreshes its on-screen bounds).
        for (el in HudManager.elements()) {
            HudManager.draw(guiGraphics, font, el, el.preview())
        }

        // Topmost element under the cursor (later draws sit on top).
        val nowHover = if (dragging != null) hovered
                       else HudManager.elements().lastOrNull { it.contains(mouseX, mouseY) }
        if (nowHover !== hovered) {
            hovered = nowHover
            hoverStartMs = System.currentTimeMillis()
        }

        // Outlines — highlight the hovered / dragged element.
        for (el in HudManager.elements()) {
            val active = el === dragging || el === hovered
            drawOutline(guiGraphics, el, if (active) ACCENT_COLOR else OUTLINE_COLOR)
        }

        // Hover tooltip: the owning feature's name, centered below the element,
        // after a short dwell.
        val h = hovered
        if (h != null && dragging == null &&
            System.currentTimeMillis() - hoverStartMs >= HOVER_DELAY_MS
        ) {
            val label = h.name
            val lw = font.width(label)
            val tx = h.lastX + (h.lastW - lw) / 2
            val ty = h.lastY + h.lastH + 4
            guiGraphics.fill(tx - 3, ty - 2, tx + lw + 3, ty + font.lineHeight + 1, TOOLTIP_BG)
            guiGraphics.text(font, label, tx, ty, NAME_COLOR, false)
        }
    }

    private fun drawOutline(g: GuiGraphicsExtractor, el: HudElement, color: Int) {
        val x1 = el.lastX
        val y1 = el.lastY
        val x2 = el.lastX + el.lastW.coerceAtLeast(1)
        val y2 = el.lastY + el.lastH.coerceAtLeast(1)
        g.fill(x1 - 1, y1 - 1, x2 + 1, y1, color)   // top
        g.fill(x1 - 1, y2, x2 + 1, y2 + 1, color)   // bottom
        g.fill(x1 - 1, y1, x1, y2, color)           // left
        g.fill(x2, y1, x2 + 1, y2, color)           // right
    }

    private fun HudElement.contains(mx: Int, my: Int): Boolean =
        mx >= lastX && mx < lastX + lastW.coerceAtLeast(1) &&
        my >= lastY && my < lastY + lastH.coerceAtLeast(1)

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()
        val hit = HudManager.elements().lastOrNull { it.contains(mx, my) }
        if (hit != null) {
            dragging = hit
            dragOffsetX = mx - hit.lastX
            dragOffsetY = my - hit.lastY
            return true
        }
        return super.mouseClicked(event, bl)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        val d = dragging ?: return super.mouseDragged(event, dx, dy)
        // width/height are the gui-scaled screen dims, matching lastX/lastY space.
        val newX = (event.x.toInt() - dragOffsetX).toFloat()
        val newY = (event.y.toInt() - dragOffsetY).toFloat()
        d.anchorX = (newX / width.toFloat()).coerceIn(0f, 1f)
        d.anchorY = (newY / height.toFloat()).coerceIn(0f, 1f)
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (dragging != null) {
            dragging = null
            FeatureManager.save()
            return true
        }
        return super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val el = HudManager.elements().lastOrNull { it.contains(mouseX.toInt(), mouseY.toInt()) }
        if (el != null && scrollY != 0.0) {
            val step = if (scrollY > 0) SCALE_STEP else -SCALE_STEP
            el.scale = (el.scale + step).coerceIn(HudElement.MIN_SCALE, HudElement.MAX_SCALE)
            FeatureManager.save()
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val h = hovered
        if (event.key == GLFW.GLFW_KEY_R && h != null) {
            h.resetToDefault()
            FeatureManager.save()
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        private const val BG_DIM = 0x99000000.toInt()
        private const val OUTLINE_COLOR = 0x66FFFFFF
        private const val TOOLTIP_BG = 0xDD000000.toInt()
        private const val HELP_COLOR = 0xFFCCCCCC.toInt()
        private const val HOVER_DELAY_MS = 400L
        private const val SCALE_STEP = 0.1f
    }
}
