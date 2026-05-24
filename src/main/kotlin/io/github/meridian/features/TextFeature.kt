package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.ACCENT_COLOR
import io.github.meridian.gui.DESC_COLOR
import io.github.meridian.gui.NAME_COLOR
import io.github.meridian.gui.ROW_BG_COLOR
import io.github.meridian.gui.ROW_HEIGHT
import io.github.meridian.gui.ROW_PADDING_X
import io.github.meridian.gui.ROW_PADDING_Y
import io.github.meridian.utils.playClickSound
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent

// A feature row with an inline text input on the right. Value is a free-form
// string the feature owns — typically read by other features (player allowlists,
// chat templates, etc.). This row has no side effects on its own.
open class TextFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    subcategory: String = "",
    val placeholder: String = "",
    val maxLength: Int = 256,
    defaultValue: String = ""
) : Feature(name, description, category, configKey, subcategory) {

    var value: String = defaultValue
        private set

    // Caret position. When != selectionAnchor, [min..max] is the selected range.
    private var cursorPos = defaultValue.length
    private var selectionAnchor = defaultValue.length
    private var scrollOffset = 0
    private var lastClickMs = 0L

    private val focused: Boolean get() = currentFocus === this
    private fun focus() { currentFocus = this }
    private fun unfocus() { if (currentFocus === this) currentFocus = null }

    private var inputX = 0
    private var inputY = 0
    private var inputW = 0

    private val selStart get() = minOf(cursorPos, selectionAnchor)
    private val selEnd   get() = maxOf(cursorPos, selectionAnchor)
    private val hasSelection get() = cursorPos != selectionAnchor

    fun setValue(newValue: String, save: Boolean = true) {
        value = newValue.take(maxLength)
        cursorPos = cursorPos.coerceIn(0, value.length)
        selectionAnchor = cursorPos
        if (save) FeatureManager.save()
    }

    override fun render(
        guiGraphics: GuiGraphics,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int {
        guiGraphics.fill(x, y, x + width, y + ROW_HEIGHT, ROW_BG_COLOR)
        guiGraphics.drawString(font, name, x + ROW_PADDING_X, y + ROW_PADDING_Y, NAME_COLOR, false)
        guiGraphics.drawString(
            font, description,
            x + ROW_PADDING_X, y + ROW_PADDING_Y + font.lineHeight + 2,
            DESC_COLOR, false
        )

        inputW = INPUT_WIDTH
        inputX = x + width - inputW - INPUT_RIGHT_PADDING
        inputY = y + (ROW_HEIGHT - INPUT_HEIGHT) / 2

        renderInput(guiGraphics, font)

        return ROW_HEIGHT
    }

    private fun renderInput(g: GuiGraphics, font: Font) {
        val bg = if (focused) 0xFF2A2A38.toInt() else 0xFF222228.toInt()
        val border = if (focused) ACCENT_COLOR else 0xFF444444.toInt()
        g.fill(inputX, inputY, inputX + inputW, inputY + INPUT_HEIGHT, bg)
        g.fill(inputX, inputY, inputX + inputW, inputY + 1, border)
        g.fill(inputX, inputY + INPUT_HEIGHT - 1, inputX + inputW, inputY + INPUT_HEIGHT, border)
        g.fill(inputX, inputY, inputX + 1, inputY + INPUT_HEIGHT, border)
        g.fill(inputX + inputW - 1, inputY, inputX + inputW, inputY + INPUT_HEIGHT, border)

        val textInsetX = inputX + 4
        val textY = inputY + (INPUT_HEIGHT - font.lineHeight) / 2 + 1
        val maxTextW = inputW - 8

        clampScrollToCursor(font, maxTextW)

        val visibleEnd = visibleEndIndex(font, maxTextW)
        val visible = if (scrollOffset >= value.length) "" else value.substring(scrollOffset, visibleEnd)

        if (focused && hasSelection) {
            val highlightStart = selStart.coerceIn(scrollOffset, visibleEnd)
            val highlightEnd = selEnd.coerceIn(scrollOffset, visibleEnd)
            if (highlightEnd > highlightStart) {
                val hx1 = textInsetX + font.width(value.substring(scrollOffset, highlightStart))
                val hx2 = textInsetX + font.width(value.substring(scrollOffset, highlightEnd))
                g.fill(hx1, inputY + 2, hx2, inputY + INPUT_HEIGHT - 2, 0x803366CC.toInt())
            }
        }

        if (value.isEmpty() && !focused && placeholder.isNotEmpty()) {
            g.drawString(font, placeholder, textInsetX, textY, 0xFF777777.toInt(), false)
        } else {
            g.drawString(font, visible, textInsetX, textY, 0xFFFFFFFF.toInt(), false)
        }

        if (focused && !hasSelection && (System.currentTimeMillis() / 500) % 2 == 0L) {
            val visibleCursorPos = (cursorPos - scrollOffset).coerceAtLeast(0)
            val cursorPx = textInsetX + font.width(visible.substring(0, visibleCursorPos.coerceAtMost(visible.length)))
            g.fill(cursorPx, inputY + 2, cursorPx + 1, inputY + INPUT_HEIGHT - 2, 0xFFFFFFFF.toInt())
        }
    }

    private fun clampScrollToCursor(font: Font, maxTextW: Int) {
        if (cursorPos < scrollOffset) {
            scrollOffset = cursorPos
        } else {
            while (scrollOffset < cursorPos &&
                font.width(value.substring(scrollOffset, cursorPos)) > maxTextW) {
                scrollOffset++
            }
        }
        if (scrollOffset > value.length) scrollOffset = value.length
    }

    private fun visibleEndIndex(font: Font, maxTextW: Int): Int {
        if (scrollOffset >= value.length) return value.length
        var end = scrollOffset
        while (end < value.length && font.width(value.substring(scrollOffset, end + 1)) <= maxTextW) {
            end++
        }
        return end
    }

    private fun charIndexAtX(font: Font, px: Int): Int {
        val textInsetX = inputX + 4
        val maxTextW = inputW - 8
        val end = visibleEndIndex(font, maxTextW)
        if (px <= textInsetX) return scrollOffset
        var i = scrollOffset
        while (i < end) {
            val left = textInsetX + font.width(value.substring(scrollOffset, i))
            val right = textInsetX + font.width(value.substring(scrollOffset, i + 1))
            if (px < (left + right) / 2) return i
            i++
        }
        return end
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        val inside = mouseX in inputX until (inputX + inputW) &&
                     mouseY in inputY until (inputY + INPUT_HEIGHT)
        if (inside) {
            if (!focused) playClickSound()
            focus()
            val now = System.currentTimeMillis()
            val isDoubleClick = (now - lastClickMs) < DOUBLE_CLICK_MS
            lastClickMs = now
            if (isDoubleClick) {
                selectionAnchor = 0
                cursorPos = value.length
                return true
            }
            val font = Minecraft.getInstance().font
            val newCursor = charIndexAtX(font, mouseX)
            // Shift+click: extend selection. Plain click: collapse caret.
            if (!hasShift()) selectionAnchor = newCursor
            cursorPos = newCursor
            return true
        }
        unfocus()
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (!focused) return false

        // Clipboard / select-all first — these don't depend on the key code.
        when {
            event.isSelectAll -> { selectionAnchor = 0; cursorPos = value.length; return true }
            event.isCopy -> { copySelection(); return true }
            event.isCut  -> { if (hasSelection) { copySelection(); replaceSelection(""); }; return true }
            event.isPaste -> {
                val pasted = Minecraft.getInstance().keyboardHandler.clipboard
                val sanitized = pasted.filter { it.code >= 0x20 && it.code != 0x7F }
                insertText(sanitized)
                return true
            }
        }

        val shift = event.hasShiftDown()
        when (event.key) {
            259 -> { // Backspace
                if (hasSelection) replaceSelection("")
                else if (cursorPos > 0) {
                    value = value.removeRange(cursorPos - 1, cursorPos)
                    cursorPos--; selectionAnchor = cursorPos
                    FeatureManager.save()
                }
            }
            261 -> { // Delete
                if (hasSelection) replaceSelection("")
                else if (cursorPos < value.length) {
                    value = value.removeRange(cursorPos, cursorPos + 1)
                    FeatureManager.save()
                }
            }
            263 -> moveCaret(cursorPos - 1, shift)                     // Left
            262 -> moveCaret(cursorPos + 1, shift)                     // Right
            268 -> moveCaret(0, shift)                                 // Home
            269 -> moveCaret(value.length, shift)                      // End
            257, 335 -> unfocus()                                      // Enter / KP-Enter
            256 -> unfocus()                                           // Escape
            else -> return false
        }
        return true
    }

    private fun moveCaret(to: Int, extendSelection: Boolean) {
        cursorPos = to.coerceIn(0, value.length)
        if (!extendSelection) selectionAnchor = cursorPos
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (!focused) return false
        val c = Character.toChars(event.codepoint).firstOrNull() ?: return false
        if (c.code < 0x20 || c.code == 0x7F) return false
        insertText(c.toString())
        return true
    }

    private fun insertText(text: String) {
        if (text.isEmpty()) return
        if (hasSelection) replaceSelection(text)
        else {
            val room = maxLength - value.length
            if (room <= 0) return
            val toInsert = if (text.length > room) text.substring(0, room) else text
            value = value.substring(0, cursorPos) + toInsert + value.substring(cursorPos)
            cursorPos += toInsert.length
            selectionAnchor = cursorPos
            FeatureManager.save()
        }
    }

    private fun replaceSelection(replacement: String) {
        val start = selStart; val end = selEnd
        val room = maxLength - (value.length - (end - start))
        val toInsert = if (replacement.length > room) replacement.substring(0, maxOf(0, room)) else replacement
        value = value.substring(0, start) + toInsert + value.substring(end)
        cursorPos = start + toInsert.length
        selectionAnchor = cursorPos
        FeatureManager.save()
    }

    private fun copySelection() {
        if (!hasSelection) return
        Minecraft.getInstance().keyboardHandler.clipboard = value.substring(selStart, selEnd)
    }

    override fun saveTo(json: JsonObject) {
        json.addProperty("value", value)
    }

    override fun loadFrom(json: JsonObject) {
        if (json.has("value")) {
            value = json.get("value").asString.take(maxLength)
            cursorPos = value.length
            selectionAnchor = cursorPos
        }
    }

    companion object {
        private const val INPUT_WIDTH = 80
        private const val INPUT_HEIGHT = 14
        private const val INPUT_RIGHT_PADDING = 8
        private const val DOUBLE_CLICK_MS = 400L

        // Only one TextFeature can be focused at a time across the whole GUI.
        private var currentFocus: TextFeature? = null

        // Allows other focusable widgets (e.g. the search bar) to steal focus.
        fun clearFocus() { currentFocus = null }

        // We can't reach Screen.hasShiftDown() from outside a Screen, but we don't
        // actually need GLFW directly — shift+arrow uses event.hasShiftDown(). For
        // shift+click we read the modifier off MouseButtonEvent if exposed; if not,
        // we fall back to the static GLFW poll below.
        private fun hasShift(): Boolean {
            val window = Minecraft.getInstance().window.handle()
            return org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS ||
                   org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
        }
    }
}