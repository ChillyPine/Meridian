package io.github.meridian.gui

import io.github.meridian.features.types.TextFeature
import io.github.meridian.utils.playClickSound
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

// Multi-line, word-wrapped text editor. Unlike [SearchBar] it lays the text out
// as a left-aligned paragraph across as many lines as needed and grows
// vertically to fit, so the whole reason stays visible while editing. The text
// is a single string (no manual newlines — Enter is left to the caller to bind);
// wrapping is purely visual, so every character maps 1:1 to an index, which
// keeps caret/selection math simple.
class TextArea(private val placeholderText: String = "") {

    var text: String = ""
        private set

    var focused: Boolean = false
        private set

    private var cursorPos = 0
    private var selectionAnchor = 0
    private var lastClickMs = 0L

    // Geometry captured on render, read by mouseClicked / vertical caret moves.
    private var boxX = 0
    private var boxY = 0
    private var boxW = 0
    private var maxTextW = 0

    private val selStart get() = minOf(cursorPos, selectionAnchor)
    private val selEnd get() = maxOf(cursorPos, selectionAnchor)
    private val hasSelection get() = cursorPos != selectionAnchor

    fun focus() {
        focused = true
        TextFeature.clearFocus()
    }

    fun unfocus() { focused = false }

    fun clear() {
        text = ""
        cursorPos = 0
        selectionAnchor = 0
    }

    fun setText(s: String) {
        text = s.take(MAX_LENGTH)
        cursorPos = text.length
        selectionAnchor = cursorPos
    }

    /** Pixel height this area needs to show all of [text] wrapped to width [w]. */
    fun heightFor(font: Font, w: Int): Int {
        val lines = layout(font, w - HPAD * 2).size.coerceAtLeast(MIN_LINES)
        return lines * font.lineHeight + VPAD * 2
    }

    fun render(g: GuiGraphicsExtractor, font: Font, x: Int, y: Int, w: Int) {
        boxX = x; boxY = y; boxW = w
        maxTextW = w - HPAD * 2
        val segs = layout(font, maxTextW)
        val visibleLines = segs.size.coerceAtLeast(MIN_LINES)
        val h = visibleLines * font.lineHeight + VPAD * 2

        val bg = if (focused) 0xFF2A2A38.toInt() else 0xFF1E1E22.toInt()
        val border = if (focused) ACCENT_COLOR else 0xFF444444.toInt()
        g.fill(x, y, x + w, y + h, bg)
        g.fill(x, y, x + w, y + 1, border)
        g.fill(x, y + h - 1, x + w, y + h, border)
        g.fill(x, y, x + 1, y + h, border)
        g.fill(x + w - 1, y, x + w, y + h, border)

        val textLeft = x + HPAD
        val textTop = y + VPAD

        g.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1)

        if (text.isEmpty() && !focused) {
            g.text(font, placeholderText, textLeft, textTop, 0xFF777777.toInt(), false)
            g.disableScissor()
            return
        }

        if (focused && hasSelection) {
            segs.forEachIndexed { j, s ->
                val a = maxOf(selStart, s.start)
                val b = minOf(selEnd, s.end)
                if (a < b) {
                    val hx1 = textLeft + font.width(text.substring(s.start, a))
                    val hx2 = textLeft + font.width(text.substring(s.start, b))
                    val hy = textTop + j * font.lineHeight
                    g.fill(hx1, hy, hx2, hy + font.lineHeight, 0x803366CC.toInt())
                }
            }
        }

        segs.forEachIndexed { j, s ->
            g.text(font, text.substring(s.start, s.end), textLeft,
                textTop + j * font.lineHeight, 0xFFFFFFFF.toInt(), false)
        }

        if (focused && !hasSelection && (System.currentTimeMillis() / 500) % 2 == 0L) {
            val (line, _) = caretLineCol(segs, cursorPos)
            val s = segs[line]
            val caretX = textLeft + font.width(text.substring(s.start, cursorPos.coerceAtMost(s.end)))
            val caretY = textTop + line * font.lineHeight
            g.fill(caretX, caretY, caretX + 1, caretY + font.lineHeight, 0xFFFFFFFF.toInt())
        }

        g.disableScissor()
    }

    fun mouseClicked(mx: Int, my: Int): Boolean {
        val font = Minecraft.getInstance().font
        val segs = layout(font, maxTextW)
        val visibleLines = segs.size.coerceAtLeast(MIN_LINES)
        val h = visibleLines * font.lineHeight + VPAD * 2
        val inside = mx in boxX until (boxX + boxW) && my in boxY until (boxY + h)
        if (!inside) {
            unfocus()
            return false
        }

        if (!focused) playClickSound()
        focus()

        val now = System.currentTimeMillis()
        val isDoubleClick = (now - lastClickMs) < DOUBLE_CLICK_MS
        lastClickMs = now
        if (isDoubleClick) {
            selectionAnchor = 0
            cursorPos = text.length
            return true
        }

        val textLeft = boxX + HPAD
        val textTop = boxY + VPAD
        val line = ((my - textTop) / font.lineHeight).coerceIn(0, segs.size - 1)
        val newCursor = indexAtXInSeg(font, segs[line], mx - textLeft)
        if (!hasShift()) selectionAnchor = newCursor
        cursorPos = newCursor
        return true
    }

    fun keyPressed(event: KeyEvent): Boolean {
        if (!focused) return false

        when {
            event.isSelectAll -> { selectionAnchor = 0; cursorPos = text.length; return true }
            event.isCopy -> { copySelection(); return true }
            event.isCut -> { if (hasSelection) { copySelection(); replaceSelection("") }; return true }
            event.isPaste -> {
                val pasted = Minecraft.getInstance().keyboardHandler.clipboard
                insertText(pasted.filter { it.code >= 0x20 && it.code != 0x7F })
                return true
            }
        }

        val shift = event.hasShiftDown()
        when (event.key) {
            259 -> { // Backspace
                if (hasSelection) replaceSelection("")
                else if (cursorPos > 0) {
                    text = text.removeRange(cursorPos - 1, cursorPos)
                    cursorPos--; selectionAnchor = cursorPos
                }
            }
            261 -> { // Delete
                if (hasSelection) replaceSelection("")
                else if (cursorPos < text.length) text = text.removeRange(cursorPos, cursorPos + 1)
            }
            263 -> moveCaret(cursorPos - 1, shift) // Left
            262 -> moveCaret(cursorPos + 1, shift) // Right
            265 -> moveVertical(-1, shift)         // Up
            264 -> moveVertical(1, shift)          // Down
            268 -> moveCaret(0, shift)             // Home
            269 -> moveCaret(text.length, shift)   // End
            else -> return false
        }
        return true
    }

    fun charTyped(event: CharacterEvent): Boolean {
        if (!focused) return false
        val c = Character.toChars(event.codepoint).firstOrNull() ?: return false
        if (c.code < 0x20 || c.code == 0x7F) return false
        insertText(c.toString())
        return true
    }

    // Wraps [text] to [maxW], returning contiguous [start, end) spans that cover
    // the whole string. Breaks at the last space that fits; if a single word is
    // wider than the line it is hard-split at the overflowing character.
    private fun layout(font: Font, maxW: Int): List<Seg> {
        val n = text.length
        if (n == 0) return listOf(Seg(0, 0))
        val res = ArrayList<Seg>()
        var start = 0
        var i = 0
        var lastBreak = -1 // index just after the most recent space in this line
        while (i < n) {
            if (font.width(text.substring(start, i + 1)) > maxW && i > start) {
                val bp = if (lastBreak in (start + 1)..i) lastBreak else i
                res.add(Seg(start, bp))
                start = bp
                lastBreak = -1
                continue // re-evaluate char i on the new line
            }
            if (text[i] == ' ') lastBreak = i + 1
            i++
        }
        res.add(Seg(start, n))
        return res
    }

    // Maps a caret index to its (line, column). At a wrap boundary the caret
    // belongs to the start of the following line so newly typed text flows there.
    private fun caretLineCol(segs: List<Seg>, idx: Int): Pair<Int, Int> {
        for (j in segs.indices) {
            val s = segs[j]
            if (idx < s.end) return j to (idx - s.start)
            if (idx == s.end) return if (j == segs.lastIndex) j to (idx - s.start) else (j + 1) to 0
        }
        val last = segs.size - 1
        return last to (idx - segs[last].start)
    }

    private fun indexAtXInSeg(font: Font, seg: Seg, targetX: Int): Int {
        if (targetX <= 0) return seg.start
        var i = seg.start
        while (i < seg.end) {
            val left = font.width(text.substring(seg.start, i))
            val right = font.width(text.substring(seg.start, i + 1))
            if (targetX < (left + right) / 2) return i
            i++
        }
        return seg.end
    }

    private fun moveCaret(to: Int, extend: Boolean) {
        cursorPos = to.coerceIn(0, text.length)
        if (!extend) selectionAnchor = cursorPos
    }

    private fun moveVertical(dir: Int, extend: Boolean) {
        val font = Minecraft.getInstance().font
        val segs = layout(font, maxTextW)
        val (line, _) = caretLineCol(segs, cursorPos)
        val caretX = font.width(text.substring(segs[line].start, cursorPos.coerceAtMost(segs[line].end)))
        val target = line + dir
        when {
            target < 0 -> moveCaret(0, extend)
            target > segs.size - 1 -> moveCaret(text.length, extend)
            else -> moveCaret(indexAtXInSeg(font, segs[target], caretX), extend)
        }
    }

    private fun insertText(s: String) {
        if (s.isEmpty()) return
        if (hasSelection) { replaceSelection(s); return }
        val room = MAX_LENGTH - text.length
        if (room <= 0) return
        val toInsert = if (s.length > room) s.substring(0, room) else s
        text = text.substring(0, cursorPos) + toInsert + text.substring(cursorPos)
        cursorPos += toInsert.length
        selectionAnchor = cursorPos
    }

    private fun replaceSelection(replacement: String) {
        val start = selStart; val end = selEnd
        val room = MAX_LENGTH - (text.length - (end - start))
        val toInsert = if (replacement.length > room) replacement.substring(0, maxOf(0, room)) else replacement
        text = text.substring(0, start) + toInsert + text.substring(end)
        cursorPos = start + toInsert.length
        selectionAnchor = cursorPos
    }

    private fun copySelection() {
        if (!hasSelection) return
        Minecraft.getInstance().keyboardHandler.clipboard = text.substring(selStart, selEnd)
    }

    private data class Seg(val start: Int, val end: Int)

    companion object {
        private const val MAX_LENGTH = 256
        private const val MIN_LINES = 3
        private const val HPAD = 4
        private const val VPAD = 4
        private const val DOUBLE_CLICK_MS = 400L

        private fun hasShift(): Boolean {
            val window = Minecraft.getInstance().window.handle()
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                   GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS
        }
    }
}
