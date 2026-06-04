package io.github.meridian.gui

import io.github.meridian.features.TextFeature
import io.github.meridian.utils.playClickSound
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent

// Standalone search input rendered below the main panel. Mirrors TextFeature's
// editing semantics (selection, shift+arrow, double-click select-all,
// Ctrl+A/C/V/X) but renders centered text and isn't persisted to config.
class SearchBar(private val placeholderText: String = "Search") {

    var query: String = ""
        private set

    var focused: Boolean = false
        private set

    private var cursorPos = 0
    private var selectionAnchor = 0
    private var lastClickMs = 0L

    private var inputX = 0
    private var inputY = 0
    private var inputW = 0

    private val selStart get() = minOf(cursorPos, selectionAnchor)
    private val selEnd   get() = maxOf(cursorPos, selectionAnchor)
    private val hasSelection get() = cursorPos != selectionAnchor

    fun focus() {
        focused = true
        TextFeature.clearFocus()
    }

    fun unfocus() { focused = false }

    fun clear() {
        query = ""
        cursorPos = 0
        selectionAnchor = 0
    }

    fun setText(text: String) {
        query = text.take(MAX_LENGTH)
        cursorPos = query.length
        selectionAnchor = cursorPos
    }

    fun render(g: GuiGraphics, font: Font, x: Int, y: Int, w: Int, h: Int) {
        inputX = x; inputY = y; inputW = w

        val bg = if (focused) 0xFF2A2A38.toInt() else 0xFF1E1E22.toInt()
        val border = if (focused) ACCENT_COLOR else 0xFF444444.toInt()
        g.fill(x, y, x + w, y + h, bg)
        g.fill(x, y, x + w, y + 1, border)
        g.fill(x, y + h - 1, x + w, y + h, border)
        g.fill(x, y, x + 1, y + h, border)
        g.fill(x + w - 1, y, x + w, y + h, border)

        val textY = y + (h - font.lineHeight) / 2 + 1
        val maxTextW = w - 8

        val displayText = if (query.isEmpty() && !focused) placeholderText else query
        val displayColor = if (query.isEmpty() && !focused) 0xFF777777.toInt() else 0xFFFFFFFF.toInt()

        val textW = font.width(displayText).coerceAtMost(maxTextW)
        val baseX = x + (w - textW) / 2

        // Use scissor so overflowing text (and selection highlight) clips to the box.
        g.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1)

        if (focused && hasSelection) {
            val hx1 = baseX + font.width(query.substring(0, selStart))
            val hx2 = baseX + font.width(query.substring(0, selEnd))
            g.fill(hx1, y + 2, hx2, y + h - 2, 0x803366CC.toInt())
        }

        g.drawString(font, displayText, baseX, textY, displayColor, false)

        if (focused && !hasSelection && (System.currentTimeMillis() / 500) % 2 == 0L) {
            val caretX = baseX + font.width(query.substring(0, cursorPos))
            g.fill(caretX, y + 2, caretX + 1, y + h - 2, 0xFFFFFFFF.toInt())
        }

        g.disableScissor()
    }

    private fun charIndexAtX(font: Font, px: Int, baseX: Int): Int {
        if (px <= baseX) return 0
        var i = 0
        while (i < query.length) {
            val left = baseX + font.width(query.substring(0, i))
            val right = baseX + font.width(query.substring(0, i + 1))
            if (px < (left + right) / 2) return i
            i++
        }
        return query.length
    }

    fun mouseClicked(mx: Int, my: Int, h: Int): Boolean {
        val inside = mx in inputX until (inputX + inputW) && my in inputY until (inputY + h)
        if (!inside) {
            unfocus()
            return false
        }

        if (!focused) playClickSound()
        focus()

        val font = Minecraft.getInstance().font
        val textW = font.width(query).coerceAtMost(inputW - 8)
        val baseX = inputX + (inputW - textW) / 2

        val now = System.currentTimeMillis()
        val isDoubleClick = (now - lastClickMs) < DOUBLE_CLICK_MS
        lastClickMs = now
        if (isDoubleClick) {
            selectionAnchor = 0
            cursorPos = query.length
            return true
        }

        val newCursor = charIndexAtX(font, mx, baseX)
        if (!hasShift()) selectionAnchor = newCursor
        cursorPos = newCursor
        return true
    }

    fun keyPressed(event: KeyEvent): Boolean {
        if (!focused) return false

        when {
            event.isSelectAll -> { selectionAnchor = 0; cursorPos = query.length; return true }
            event.isCopy -> { copySelection(); return true }
            event.isCut  -> { if (hasSelection) { copySelection(); replaceSelection("") }; return true }
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
                    query = query.removeRange(cursorPos - 1, cursorPos)
                    cursorPos--; selectionAnchor = cursorPos
                }
            }
            261 -> { // Delete
                if (hasSelection) replaceSelection("")
                else if (cursorPos < query.length) {
                    query = query.removeRange(cursorPos, cursorPos + 1)
                }
            }
            263 -> moveCaret(cursorPos - 1, shift)
            262 -> moveCaret(cursorPos + 1, shift)
            268 -> moveCaret(0, shift)
            269 -> moveCaret(query.length, shift)
            257, 335 -> unfocus()
            256 -> {
                if (query.isNotEmpty()) clear() else unfocus()
            }
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

    private fun moveCaret(to: Int, extend: Boolean) {
        cursorPos = to.coerceIn(0, query.length)
        if (!extend) selectionAnchor = cursorPos
    }

    private fun insertText(text: String) {
        if (text.isEmpty()) return
        if (hasSelection) { replaceSelection(text); return }
        val room = MAX_LENGTH - query.length
        if (room <= 0) return
        val toInsert = if (text.length > room) text.substring(0, room) else text
        query = query.substring(0, cursorPos) + toInsert + query.substring(cursorPos)
        cursorPos += toInsert.length
        selectionAnchor = cursorPos
    }

    private fun replaceSelection(replacement: String) {
        val start = selStart; val end = selEnd
        val room = MAX_LENGTH - (query.length - (end - start))
        val toInsert = if (replacement.length > room) replacement.substring(0, maxOf(0, room)) else replacement
        query = query.substring(0, start) + toInsert + query.substring(end)
        cursorPos = start + toInsert.length
        selectionAnchor = cursorPos
    }

    private fun copySelection() {
        if (!hasSelection) return
        Minecraft.getInstance().keyboardHandler.clipboard = query.substring(selStart, selEnd)
    }

    companion object {
        const val HEIGHT = 16
        private const val MAX_LENGTH = 64
        private const val DOUBLE_CLICK_MS = 400L

        private fun hasShift(): Boolean {
            val window = Minecraft.getInstance().window.handle()
            return org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS ||
                   org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
        }
    }
}
