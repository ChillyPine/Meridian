package io.github.meridian.gui

import io.github.meridian.features.impl.dungeons.ShitterList
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

// Opened with `/md shitter gui` (and the "Open GUI" button in Dungeons). A
// centered panel with an add-input + Add button on top and a scrollable list of
// names below, each with a red remove button. Mutates the shared [ShitterList]
// store directly, so changes here and via `/md shitter` stay in sync.
class ShitterListScreen : Screen(Component.literal("Shitter List")) {

    private val input = SearchBar(placeholderText = "Type a name, then Add")

    // Layout, recomputed every render and read by the click/drag/scroll handlers.
    private var panelX = 0
    private var panelY = 0
    private var addBtnX = 0
    private var addBtnY = 0
    private var addBtnW = 0
    private var addBtnH = 0
    private var listLeft = 0
    private var listRight = 0
    private var listTop = 0
    private var listBottom = 0

    private var scrollOffset = 0
    private var maxScroll = 0
    private var scrollbarVisible = false
    private var scrollbarX = 0
    private var thumbY = 0
    private var thumbH = 0
    private var draggingThumb = false
    private var thumbDragOffsetY = 0

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Dim the world behind the panel (the default blur is skipped).
        guiGraphics.fill(0, 0, width, height, BG_DIM)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        panelX = (width - PANEL_WIDTH) / 2
        panelY = (height - PANEL_HEIGHT) / 2
        val panelColor = (PANEL_OPACITY shl 24) or PANEL_COLOR
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, panelColor)
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, ACCENT_COLOR)

        val title = "§lShitter List"
        guiGraphics.drawString(font, title, panelX + (PANEL_WIDTH - font.width(title)) / 2, panelY + 8, NAME_COLOR, false)

        // Input + Add button row.
        val inputY = panelY + 24
        addBtnW = font.width(ADD_LABEL) + 16
        addBtnH = SearchBar.HEIGHT
        addBtnX = panelX + PANEL_WIDTH - PADDING - addBtnW
        addBtnY = inputY
        val inputX = panelX + PADDING
        val inputW = addBtnX - INPUT_BTN_GAP - inputX
        input.render(guiGraphics, font, inputX, inputY, inputW, SearchBar.HEIGHT)

        val addHovered = mouseX in addBtnX until (addBtnX + addBtnW) && mouseY in addBtnY until (addBtnY + addBtnH)
        guiGraphics.fill(addBtnX, addBtnY, addBtnX + addBtnW, addBtnY + addBtnH,
            if (addHovered) ACCENT_HOVER else ACCENT_COLOR)
        guiGraphics.drawString(font, ADD_LABEL,
            addBtnX + (addBtnW - font.width(ADD_LABEL)) / 2,
            addBtnY + (addBtnH - font.lineHeight) / 2 + 1, NAME_COLOR, false)

        // Count line.
        val names = ShitterList.all()
        val countY = inputY + addBtnH + 6
        guiGraphics.drawString(font, "§7${names.size} player${if (names.size == 1) "" else "s"}",
            panelX + PADDING, countY, DESC_COLOR, false)

        // List area geometry.
        listLeft = panelX + PADDING
        listTop = countY + font.lineHeight + 4
        listBottom = panelY + PANEL_HEIGHT - PADDING
        val fullRight = panelX + PANEL_WIDTH - PADDING

        val viewportH = listBottom - listTop
        val contentH = names.size * ROW_H
        maxScroll = maxOf(0, contentH - viewportH)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
        scrollbarVisible = contentH > viewportH
        listRight = if (scrollbarVisible) fullRight - SCROLLBAR_W - 2 else fullRight

        if (names.isEmpty()) {
            val empty = "§7List is empty — add a player above."
            guiGraphics.drawString(font, empty,
                listLeft + (listRight - listLeft - font.width(empty)) / 2, listTop + 6, DESC_COLOR, false)
            return
        }

        guiGraphics.enableScissor(listLeft, listTop, listRight, listBottom)
        names.forEachIndexed { i, name ->
            val rowTop = listTop - scrollOffset + i * ROW_H
            if (rowTop + ROW_H < listTop || rowTop > listBottom) return@forEachIndexed

            guiGraphics.fill(listLeft, rowTop, listRight, rowTop + ROW_H - 1,
                if (i % 2 == 0) ROW_BG_COLOR else ROW_BG_COLOR_ALT)
            guiGraphics.drawString(font, "§f$name", listLeft + 6,
                rowTop + (ROW_H - font.lineHeight) / 2 + 1, NAME_COLOR, false)

            val rbX = listRight - REMOVE_SIZE - 4
            val rbY = rowTop + (ROW_H - REMOVE_SIZE) / 2
            val rbHovered = mouseX in rbX until (rbX + REMOVE_SIZE) &&
                            mouseY in rbY until (rbY + REMOVE_SIZE) &&
                            mouseY in listTop until listBottom
            guiGraphics.fill(rbX, rbY, rbX + REMOVE_SIZE, rbY + REMOVE_SIZE,
                if (rbHovered) REMOVE_COLOR_HOVER else REMOVE_COLOR)
            guiGraphics.drawString(font, REMOVE_GLYPH,
                rbX + (REMOVE_SIZE - font.width(REMOVE_GLYPH)) / 2 + 1,
                rbY + (REMOVE_SIZE - font.lineHeight) / 2 + 1, NAME_COLOR, false)
        }
        guiGraphics.disableScissor()

        if (scrollbarVisible) renderScrollbar(guiGraphics, fullRight - SCROLLBAR_W, viewportH, contentH, mouseX, mouseY)
    }

    private fun renderScrollbar(g: GuiGraphics, trackX: Int, viewportH: Int, contentH: Int, mouseX: Int, mouseY: Int) {
        scrollbarX = trackX
        g.fill(trackX, listTop, trackX + SCROLLBAR_W, listBottom, SCROLLBAR_TRACK)
        thumbH = (viewportH.toLong() * viewportH / contentH).toInt().coerceAtLeast(MIN_THUMB_H).coerceAtMost(viewportH)
        val travel = viewportH - thumbH
        thumbY = if (maxScroll == 0) listTop else listTop + (scrollOffset.toLong() * travel / maxScroll).toInt()
        val hover = mouseX in trackX..(trackX + SCROLLBAR_W) && mouseY in thumbY..(thumbY + thumbH)
        g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH,
            if (hover || draggingThumb) ACCENT_HOVER else ACCENT_COLOR)
    }

    private fun submitAdd() {
        val raw = input.query.trim()
        if (raw.isEmpty()) return
        raw.split(Regex("\\s+")).filter { it.isNotEmpty() }.forEach { ShitterList.add(it) }
        input.clear()
    }

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()

        if (input.mouseClicked(mx, my, SearchBar.HEIGHT)) return true

        if (mx in addBtnX until (addBtnX + addBtnW) && my in addBtnY until (addBtnY + addBtnH)) {
            submitAdd()
            playClickSound()
            return true
        }

        if (scrollbarVisible && mx in scrollbarX..(scrollbarX + SCROLLBAR_W) && my in listTop..listBottom) {
            if (my in thumbY..(thumbY + thumbH)) {
                draggingThumb = true
                thumbDragOffsetY = my - thumbY
            } else {
                val dir = if (my < thumbY) -1 else 1
                scrollOffset = (scrollOffset + dir * (listBottom - listTop)).coerceIn(0, maxScroll)
            }
            return true
        }

        if (my in listTop until listBottom) {
            val names = ShitterList.all()
            names.forEachIndexed { i, name ->
                val rowTop = listTop - scrollOffset + i * ROW_H
                val rbX = listRight - REMOVE_SIZE - 4
                val rbY = rowTop + (ROW_H - REMOVE_SIZE) / 2
                if (mx in rbX until (rbX + REMOVE_SIZE) && my in rbY until (rbY + REMOVE_SIZE)) {
                    ShitterList.remove(name)
                    playClickSound()
                    return true
                }
            }
        }

        return super.mouseClicked(event, bl)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingThumb) {
            draggingThumb = false
            return true
        }
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (draggingThumb && maxScroll > 0) {
            val viewportH = listBottom - listTop
            val travel = (viewportH - thumbH).coerceAtLeast(1)
            val newThumbTop = event.y.toInt() - thumbDragOffsetY - listTop
            scrollOffset = (newThumbTop.toLong() * maxScroll / travel).toInt().coerceIn(0, maxScroll)
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val inPanel = mouseX.toInt() in panelX..(panelX + PANEL_WIDTH) &&
                      mouseY.toInt() in panelY..(panelY + PANEL_HEIGHT)
        if (maxScroll > 0 && inPanel && scrollY != 0.0) {
            scrollOffset = (scrollOffset - (scrollY * ROW_H).toInt()).coerceIn(0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        // Enter submits the add field instead of just unfocusing it.
        if (input.focused && (event.key == GLFW.GLFW_KEY_ENTER || event.key == GLFW.GLFW_KEY_KP_ENTER)) {
            submitAdd()
            return true
        }
        if (input.keyPressed(event)) return true
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (input.charTyped(event)) return true
        return super.charTyped(event)
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        private const val PANEL_WIDTH = 260
        private const val PANEL_HEIGHT = 230
        private const val PADDING = 10
        private const val INPUT_BTN_GAP = 6
        private const val ADD_LABEL = "Add"

        private const val PANEL_COLOR = 0x1E1E22       // RGB only
        private const val PANEL_OPACITY = 210
        private const val BG_DIM = 0x99000000.toInt()

        private const val ROW_H = 16
        private const val ROW_BG_COLOR_ALT = 0x33000000
        private const val ACCENT_HOVER = 0xFFD0A6FF.toInt()
        private const val REMOVE_SIZE = 12
        private const val REMOVE_COLOR = 0xFFB04444.toInt()
        private const val REMOVE_COLOR_HOVER = 0xFFE05555.toInt()
        private const val REMOVE_GLYPH = "x"

        private const val SCROLLBAR_W = 4
        private const val SCROLLBAR_TRACK = 0x55000000
        private const val MIN_THUMB_H = 16
    }
}
