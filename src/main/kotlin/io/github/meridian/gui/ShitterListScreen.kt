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

    // Reason side-panel state. [hoveredName] is the row the mouse is currently
    // over (transient — recomputed every frame, drives the read-only preview).
    // [editingName] is sticky: set when a row's IGN is clicked, opens the editor
    // and takes precedence over the hover preview until saved/closed.
    private val reasonArea = TextArea(placeholderText = "Type a reason")
    private var hoveredName: String? = null
    private var editingName: String? = null

    // Side-panel layout, recomputed every render, read by the click handler.
    private var sideX = 0
    private var sideY = 0
    private var sideW = 0
    private var saveBtnX = 0
    private var saveBtnY = 0
    private var saveBtnW = 0
    private var saveBtnH = 0
    private var closeBtnX = 0
    private var closeBtnY = 0

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

        hoveredName = null
        guiGraphics.enableScissor(listLeft, listTop, listRight, listBottom)
        names.forEachIndexed { i, name ->
            val rowTop = listTop - scrollOffset + i * ROW_H
            if (rowTop + ROW_H < listTop || rowTop > listBottom) return@forEachIndexed

            val rbX = listRight - REMOVE_SIZE - 4
            val rbY = rowTop + (ROW_H - REMOVE_SIZE) / 2

            // Hover over the IGN region (the row, excluding the remove button).
            val nameHovered = mouseX in listLeft until rbX &&
                              mouseY in rowTop until (rowTop + ROW_H) &&
                              mouseY in listTop until listBottom
            if (nameHovered) hoveredName = name

            guiGraphics.fill(listLeft, rowTop, listRight, rowTop + ROW_H - 1,
                if (i % 2 == 0) ROW_BG_COLOR else ROW_BG_COLOR_ALT)
            if (nameHovered || editingName.equals(name, ignoreCase = true)) {
                guiGraphics.fill(listLeft, rowTop, listRight, rowTop + ROW_H - 1, HOVER_COLOR)
            }
            guiGraphics.drawString(font, "§f$name", listLeft + 6,
                rowTop + (ROW_H - font.lineHeight) / 2 + 1, NAME_COLOR, false)

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

        // Right-side reason panel: editor wins over the transient hover preview.
        val editing = editingName
        if (editing != null) renderReasonEditor(guiGraphics, editing, mouseX, mouseY)
        else hoveredName?.let { renderReasonPreview(guiGraphics, it) }
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

    // Read-only reason shown while the mouse is over a row. Sizes to its content
    // and vanishes the instant the mouse leaves (driven by [hoveredName]).
    private fun renderReasonPreview(g: GuiGraphics, name: String) {
        sideW = SIDE_WIDTH
        sideX = panelX + PANEL_WIDTH + SIDE_GAP
        sideY = panelY
        val innerW = sideW - PADDING * 2
        val contentX = sideX + PADDING

        val reason = ShitterList.reasonFor(name)
        val lines = if (reason == null) listOf("§8No reason set.") else wrap(reason, innerW)

        val bodyTop = sideY + 8 + font.lineHeight + 4
        val sideH = (bodyTop - sideY) + lines.size * font.lineHeight + PADDING

        drawSidePanelBg(g, sideH)
        g.drawString(font, "§b$name", contentX, sideY + 8, NAME_COLOR, false)
        lines.forEachIndexed { i, line ->
            g.drawString(font, line, contentX, bodyTop + i * font.lineHeight, DESC_COLOR, false)
        }
    }

    // Sticky editor opened by clicking a row's IGN: a reason input + Save button
    // plus a close (x) in the corner.
    private fun renderReasonEditor(g: GuiGraphics, name: String, mouseX: Int, mouseY: Int) {
        sideW = SIDE_WIDTH
        sideX = panelX + PANEL_WIDTH + SIDE_GAP
        sideY = panelY
        val innerW = sideW - PADDING * 2
        val contentX = sideX + PADDING

        val titleY = sideY + 8
        val nameY = titleY + font.lineHeight + 2
        val areaX = contentX
        val areaY = nameY + font.lineHeight + 6
        val areaW = innerW
        val areaH = reasonArea.heightFor(font, areaW)
        saveBtnW = font.width(SAVE_LABEL) + 16
        saveBtnH = SearchBar.HEIGHT
        saveBtnX = sideX + sideW - PADDING - saveBtnW
        saveBtnY = areaY + areaH + 8
        val sideH = (saveBtnY + saveBtnH + PADDING) - sideY

        drawSidePanelBg(g, sideH)

        g.drawString(font, "§lEdit Reason", contentX, titleY, NAME_COLOR, false)
        g.drawString(font, "§b$name", contentX, nameY, ACCENT_COLOR, false)
        reasonArea.render(g, font, areaX, areaY, areaW)

        val saveHovered = mouseX in saveBtnX until (saveBtnX + saveBtnW) &&
                          mouseY in saveBtnY until (saveBtnY + saveBtnH)
        g.fill(saveBtnX, saveBtnY, saveBtnX + saveBtnW, saveBtnY + saveBtnH,
            if (saveHovered) ACCENT_HOVER else ACCENT_COLOR)
        g.drawString(font, SAVE_LABEL,
            saveBtnX + (saveBtnW - font.width(SAVE_LABEL)) / 2,
            saveBtnY + (saveBtnH - font.lineHeight) / 2 + 1, NAME_COLOR, false)

        // Close (x) button, top-right corner.
        closeBtnX = sideX + sideW - CLOSE_SIZE - 4
        closeBtnY = sideY + 4
        val closeHovered = mouseX in closeBtnX until (closeBtnX + CLOSE_SIZE) &&
                           mouseY in closeBtnY until (closeBtnY + CLOSE_SIZE)
        g.drawString(font, REMOVE_GLYPH, closeBtnX + 2, closeBtnY + 1,
            if (closeHovered) REMOVE_COLOR_HOVER else DESC_COLOR, false)
    }

    private fun drawSidePanelBg(g: GuiGraphics, sideH: Int) {
        val panelColor = (PANEL_OPACITY shl 24) or PANEL_COLOR
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, panelColor)
        g.fill(sideX, sideY, sideX + sideW, sideY + 2, ACCENT_COLOR)
    }

    // Greedy word-wrap; long single words simply overflow the (generous) width.
    private fun wrap(text: String, maxW: Int): List<String> {
        val lines = mutableListOf<String>()
        var cur = StringBuilder()
        for (word in text.split(" ")) {
            val candidate = if (cur.isEmpty()) word else "$cur $word"
            if (font.width(candidate) > maxW && cur.isNotEmpty()) {
                lines += cur.toString()
                cur = StringBuilder(word)
            } else {
                cur = StringBuilder(candidate)
            }
        }
        if (cur.isNotEmpty()) lines += cur.toString()
        return lines
    }

    private fun openEdit(name: String) {
        editingName = name
        reasonArea.setText(ShitterList.reasonFor(name) ?: "")
        reasonArea.focus()
        input.unfocus()
    }

    private fun closeEdit() {
        editingName = null
        reasonArea.clear()
        reasonArea.unfocus()
    }

    private fun saveReason() {
        val name = editingName ?: return
        ShitterList.setReason(name, reasonArea.text)
        closeEdit()
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

        // Reason editor (when open) gets first crack at the side-panel region.
        if (editingName != null) {
            if (mx in closeBtnX until (closeBtnX + CLOSE_SIZE) && my in closeBtnY until (closeBtnY + CLOSE_SIZE)) {
                closeEdit()
                playClickSound()
                return true
            }
            if (mx in saveBtnX until (saveBtnX + saveBtnW) && my in saveBtnY until (saveBtnY + saveBtnH)) {
                saveReason()
                playClickSound()
                return true
            }
            if (reasonArea.mouseClicked(mx, my)) return true
        }

        if (input.mouseClicked(mx, my, SearchBar.HEIGHT)) {
            // Focusing the add field hands keyboard control back to it.
            if (editingName != null) closeEdit()
            return true
        }

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
                if (rowTop + ROW_H < listTop || rowTop > listBottom) return@forEachIndexed
                val rbX = listRight - REMOVE_SIZE - 4
                val rbY = rowTop + (ROW_H - REMOVE_SIZE) / 2
                if (mx in rbX until (rbX + REMOVE_SIZE) && my in rbY until (rbY + REMOVE_SIZE)) {
                    if (editingName.equals(name, ignoreCase = true)) closeEdit()
                    ShitterList.remove(name)
                    playClickSound()
                    return true
                }
                // Click on the IGN itself opens (or switches to) the reason editor.
                if (mx in listLeft until rbX && my in rowTop until (rowTop + ROW_H)) {
                    openEdit(name)
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
        // While the reason editor is open it captures keys so Esc closes the
        // editor (not the whole screen) and Enter saves.
        if (editingName != null) {
            when (event.key) {
                GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> { saveReason(); return true }
                GLFW.GLFW_KEY_ESCAPE -> { closeEdit(); return true }
                else -> { reasonArea.keyPressed(event); return true }
            }
        }
        // Enter submits the add field instead of just unfocusing it.
        if (input.focused && (event.key == GLFW.GLFW_KEY_ENTER || event.key == GLFW.GLFW_KEY_KP_ENTER)) {
            submitAdd()
            return true
        }
        if (input.keyPressed(event)) return true
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (editingName != null) {
            reasonArea.charTyped(event)
            return true
        }
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

        // Reason side panel.
        private const val SIDE_GAP = 6
        private const val SIDE_WIDTH = 150
        private const val SAVE_LABEL = "Save"
        private const val CLOSE_SIZE = 12
    }
}
