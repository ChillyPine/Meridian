package io.github.meridian.gui

import io.github.meridian.features.impl.general.ChatBlockerEntry
import io.github.meridian.features.impl.general.ChatBlockerRegistry
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

// Opened from the "Chat Blockers" button in General (or `/md blockers`). Wider than
// ShitterListScreen because the rows show whole Hypixel chat lines. Read-only list —
// entries come from the ENTRIES table in features/impl/general/ChatBlockers.kt; the
// only interaction is the per-row checkbox.
class ChatBlockerScreen : Screen(Component.literal("Chat Blockers")) {

    // A flattened render/hit-test row: either a group header or a blocker entry.
    // Rebuilt every frame so entry heights track the current font metrics.
    private sealed interface Row {
        val height: Int

        data class Header(val title: String) : Row {
            override val height get() = HEADER_HEIGHT
        }

        data class Entry(
            val entry: ChatBlockerEntry,
            val labelLines: List<String>,
            val noteLines: List<String>,
            override val height: Int,
        ) : Row
    }

    private var panelX = 0
    private var panelY = 0
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

    override fun extractBackground(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fill(0, 0, width, height, BG_DIM)
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick)

        panelX = (width - PANEL_WIDTH) / 2
        panelY = (height - PANEL_HEIGHT) / 2
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, (PANEL_OPACITY shl 24) or PANEL_COLOR)
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, ACCENT_COLOR)

        val title = "§lChat Blockers"
        guiGraphics.text(font, title, panelX + (PANEL_WIDTH - font.width(title)) / 2, panelY + 8, NAME_COLOR, false)

        listLeft = panelX + PADDING
        listTop = panelY + 8 + font.lineHeight + 8
        listBottom = panelY + PANEL_HEIGHT - PADDING
        val fullRight = panelX + PANEL_WIDTH - PADDING

        val rows = buildRows()
        val viewportH = listBottom - listTop
        val contentH = rows.sumOf { it.height }
        maxScroll = maxOf(0, contentH - viewportH)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
        scrollbarVisible = contentH > viewportH
        listRight = if (scrollbarVisible) fullRight - SCROLLBAR_W - 2 else fullRight

        guiGraphics.enableScissor(listLeft, listTop, listRight, listBottom)
        forEachRow(rows) { row, rowTop ->
            if (rowTop + row.height < listTop || rowTop > listBottom) return@forEachRow
            when (row) {
                is Row.Header -> renderHeader(guiGraphics, row.title, rowTop)
                is Row.Entry -> renderEntry(guiGraphics, row, rowTop, mouseX, mouseY)
            }
        }
        guiGraphics.disableScissor()

        if (scrollbarVisible) renderScrollbar(guiGraphics, fullRight - SCROLLBAR_W, viewportH, contentH, mouseX, mouseY)
    }

    /** Text column stops short of the checkbox, so wrapped lines never run under it. */
    private fun textWidth(): Int =
        (listRight - listLeft) - CHECKBOX_SIZE - CHECKBOX_RIGHT_PAD - TEXT_LEFT_PAD - TEXT_RIGHT_GAP

    private fun buildRows(): List<Row> {
        val rows = mutableListOf<Row>()
        val textW = textWidth()
        for ((group, entries) in ChatBlockerRegistry.grouped()) {
            rows += Row.Header(group)
            for (entry in entries) {
                val labelLines = wrap(entry.label, textW)
                val noteLines = if (entry.note.isEmpty()) emptyList() else wrap(entry.note, textW)
                var h = ENTRY_TOP_PAD + labelLines.size * font.lineHeight + ENTRY_BOTTOM_PAD + ENTRY_GAP
                if (noteLines.isNotEmpty()) h += NOTE_TOP_GAP + noteLines.size * font.lineHeight
                rows += Row.Entry(entry, labelLines, noteLines, h)
            }
        }
        return rows
    }

    private inline fun forEachRow(rows: List<Row>, action: (Row, Int) -> Unit) {
        var y = listTop - scrollOffset
        for (row in rows) {
            action(row, y)
            y += row.height
        }
    }

    private fun renderHeader(g: GuiGraphicsExtractor, title: String, rowTop: Int) {
        val y = rowTop + HEADER_TOP_GAP
        g.text(font, title, listLeft + (listRight - listLeft - font.width(title)) / 2, y, ACCENT_COLOR, false)
        val lineY = y + font.lineHeight + 2
        g.fill(listLeft + HEADER_LINE_INSET, lineY, listRight - HEADER_LINE_INSET, lineY + 1, ACCENT_COLOR)
    }

    private fun renderEntry(g: GuiGraphicsExtractor, row: Row.Entry, rowTop: Int, mouseX: Int, mouseY: Int) {
        val entry = row.entry
        val rowBottom = rowTop + row.height - ENTRY_GAP
        val hovered = mouseX in listLeft until listRight &&
                      mouseY in rowTop until rowBottom &&
                      mouseY in listTop until listBottom

        g.fill(listLeft, rowTop, listRight, rowBottom, ROW_BG_COLOR)
        if (hovered) g.fill(listLeft, rowTop, listRight, rowBottom, HOVER_COLOR)

        val textX = listLeft + TEXT_LEFT_PAD
        var textY = rowTop + ENTRY_TOP_PAD
        for (line in row.labelLines) {
            g.text(font, line, textX, textY, NAME_COLOR, false)
            textY += font.lineHeight
        }
        if (row.noteLines.isNotEmpty()) {
            textY += NOTE_TOP_GAP
            for (line in row.noteLines) {
                g.text(font, line, textX, textY, DESC_COLOR, false)
                textY += font.lineHeight
            }
        }

        val (cbX, cbY) = checkboxPos(rowTop, row.height)
        g.fill(cbX, cbY, cbX + CHECKBOX_SIZE, cbY + CHECKBOX_SIZE,
            if (hovered) CHECKBOX_BORDER_HOVER else CHECKBOX_BORDER)
        g.fill(cbX + 1, cbY + 1, cbX + CHECKBOX_SIZE - 1, cbY + CHECKBOX_SIZE - 1, CHECKBOX_BG)
        if (entry.enabled) {
            g.fill(cbX + 3, cbY + 3, cbX + CHECKBOX_SIZE - 3, cbY + CHECKBOX_SIZE - 3, ACCENT_COLOR)
        }
    }

    private fun checkboxPos(rowTop: Int, rowHeight: Int): Pair<Int, Int> {
        val x = listRight - CHECKBOX_RIGHT_PAD - CHECKBOX_SIZE
        val y = rowTop + ((rowHeight - ENTRY_GAP) - CHECKBOX_SIZE) / 2
        return x to y
    }

    private fun renderScrollbar(g: GuiGraphicsExtractor, trackX: Int, viewportH: Int, contentH: Int, mouseX: Int, mouseY: Int) {
        scrollbarX = trackX
        g.fill(trackX, listTop, trackX + SCROLLBAR_W, listBottom, SCROLLBAR_TRACK)
        thumbH = (viewportH.toLong() * viewportH / contentH).toInt().coerceAtLeast(MIN_THUMB_H).coerceAtMost(viewportH)
        val travel = viewportH - thumbH
        thumbY = if (maxScroll == 0) listTop else listTop + (scrollOffset.toLong() * travel / maxScroll).toInt()
        val hover = mouseX in trackX..(trackX + SCROLLBAR_W) && mouseY in thumbY..(thumbY + thumbH)
        g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH,
            if (hover || draggingThumb) ACCENT_HOVER else ACCENT_COLOR)
    }

    // Greedy word-wrap. A single word wider than the column is hard-broken mid-word so
    // nothing can ever spill under the checkbox.
    private fun wrap(text: String, maxW: Int): List<String> {
        if (maxW <= 0) return listOf(text)
        val lines = mutableListOf<String>()
        var cur = StringBuilder()

        fun flush() {
            if (cur.isNotEmpty()) {
                lines += cur.toString()
                cur = StringBuilder()
            }
        }

        for (word in text.split(" ")) {
            val candidate = if (cur.isEmpty()) word else "$cur $word"
            if (font.width(candidate) <= maxW) {
                cur = StringBuilder(candidate)
                continue
            }
            flush()
            if (font.width(word) <= maxW) {
                cur = StringBuilder(word)
                continue
            }
            for (ch in word) {
                if (font.width(cur.toString() + ch) > maxW && cur.isNotEmpty()) flush()
                cur.append(ch)
            }
        }
        flush()
        return lines
    }

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()

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

        if (my in listTop until listBottom && mx in listLeft until listRight) {
            var hit: ChatBlockerEntry? = null
            forEachRow(buildRows()) { row, rowTop ->
                if (hit != null || row !is Row.Entry) return@forEachRow
                // Anywhere on the row toggles — the checkbox is a small target.
                if (my in rowTop until (rowTop + row.height - ENTRY_GAP)) hit = row.entry
            }
            hit?.let {
                it.toggle()
                playClickSound()
                return true
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
            scrollOffset = (scrollOffset - (scrollY * WHEEL_STEP_PX).toInt()).coerceIn(0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        // Wider than ShitterListScreen: rows carry whole chat lines, not IGNs.
        private const val PANEL_WIDTH = 440
        private const val PANEL_HEIGHT = 250
        private const val PADDING = 10

        private const val PANEL_COLOR = 0x1E1E22       // RGB only
        private const val PANEL_OPACITY = 210
        private const val BG_DIM = 0x99000000.toInt()

        private const val HEADER_HEIGHT = 20
        private const val HEADER_TOP_GAP = 4
        private const val HEADER_LINE_INSET = 12

        private const val ENTRY_GAP = 2                // vertical gap between row backgrounds
        private const val ENTRY_TOP_PAD = 4
        private const val ENTRY_BOTTOM_PAD = 3
        private const val NOTE_TOP_GAP = 1
        private const val TEXT_LEFT_PAD = 6
        private const val TEXT_RIGHT_GAP = 6

        private const val CHECKBOX_SIZE = 12
        private const val CHECKBOX_RIGHT_PAD = 6
        private const val CHECKBOX_BORDER = 0xFF555555.toInt()
        private const val CHECKBOX_BORDER_HOVER = 0xFF888888.toInt()
        private const val CHECKBOX_BG = 0xFF2A2A2E.toInt()

        private const val ACCENT_HOVER = 0xFFD0A6FF.toInt()
        private const val SCROLLBAR_W = 4
        private const val SCROLLBAR_TRACK = 0x55000000
        private const val MIN_THUMB_H = 16
        private const val WHEEL_STEP_PX = 20
    }
}
