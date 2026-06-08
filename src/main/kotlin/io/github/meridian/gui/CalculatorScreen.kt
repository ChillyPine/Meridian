package io.github.meridian.gui

import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

// opened with `/md calc`

class CalculatorScreen : Screen(Component.literal("Calculator")) {

    // display state.
    private var displayValue = "0"
    private var expressionLine = ""
    private var pendingOp: Char? = null
    private var pendingLeft: Double = 0.0
    private var freshInput = true

    private var panelX = 0
    private var panelY = 0
    private data class BtnRect(val label: String, val x: Int, val y: Int, val w: Int, val h: Int, val isOp: Boolean)
    private val buttons = mutableListOf<BtnRect>()

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fill(0, 0, width, height, BG_DIM)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        buttons.clear()

        panelX = (width - PANEL_WIDTH) / 2
        panelY = (height - PANEL_HEIGHT) / 2
        val panelColor = (PANEL_OPACITY shl 24) or PANEL_COLOR
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, panelColor)
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, ACCENT_COLOR)

        val title = "§lCalculator"
        guiGraphics.drawString(font, title,
            panelX + (PANEL_WIDTH - font.width(title)) / 2, panelY + 7, NAME_COLOR, false)

        val dispX = panelX + PADDING
        val dispY = panelY + 20
        val dispW = PANEL_WIDTH - PADDING * 2
        val dispH = DISPLAY_H
        guiGraphics.fill(dispX, dispY, dispX + dispW, dispY + dispH, DISPLAY_BG)

        guiGraphics.drawString(font, "§7$expressionLine",
            dispX + dispW - font.width(expressionLine) - 4,
            dispY + 4, DESC_COLOR, false)

        val valStr = displayValue
        guiGraphics.drawString(font, "§f$valStr",
            dispX + dispW - font.width(valStr) - 4,
            dispY + dispH - font.lineHeight - 4, NAME_COLOR, false)

        val gridTop = dispY + dispH + GRID_GAP
        val gridLeft = panelX + PADDING
        val totalW = PANEL_WIDTH - PADDING * 2
        val cellW = (totalW - BTN_GAP * 3) / 4
        val cellH = BTN_H
        val rows: List<List<Triple<String, Int, Boolean>>> = listOf(
            listOf(Triple("AC", 3, false), Triple("÷", 1, true)),
            listOf(Triple("7", 1, false), Triple("8", 1, false), Triple("9", 1, false), Triple("×", 1, true)),
            listOf(Triple("4", 1, false), Triple("5", 1, false), Triple("6", 1, false), Triple("−", 1, true)),
            listOf(Triple("1", 1, false), Triple("2", 1, false), Triple("3", 1, false), Triple("+", 1, true)),
            listOf(Triple(".", 1, false), Triple("0", 1, false), Triple("⌫", 1, false), Triple("=", 1, true)),
            listOf(Triple("Thousand", 1, false), Triple("Million", 1, false), Triple("Billion", 1, false))
        )

        rows.forEachIndexed { rowIdx, row ->
            var colCursor = 0
            row.forEach { (label, span, isOp) ->
                val bx = gridLeft + colCursor * (cellW + BTN_GAP)
                val by = gridTop + rowIdx * (cellH + BTN_GAP)
                val bw = cellW * span + BTN_GAP * (span - 1)
                val hovered = mouseX in bx until (bx + bw) && mouseY in by until (by + cellH)
                val bg = when {
                    isOp && hovered -> ACCENT_HOVER
                    isOp            -> ACCENT_COLOR
                    hovered         -> BTN_HOVER
                    else            -> BTN_BG
                }
                guiGraphics.fill(bx, by, bx + bw, by + cellH, bg)
                guiGraphics.drawString(font, label,
                    bx + (bw - font.width(label)) / 2,
                    by + (cellH - font.lineHeight) / 2 + 1,
                    NAME_COLOR, false)
                buttons += BtnRect(label, bx, by, bw, cellH, isOp)
                colCursor += span
            }
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()
        buttons.forEach { btn ->
            if (mx in btn.x until (btn.x + btn.w) && my in btn.y until (btn.y + btn.h)) {
                handleButton(btn.label)
                playClickSound()
                return true
            }
        }
        return super.mouseClicked(event, bl)
    }

    // keyboard dogshit

    override fun keyPressed(event: KeyEvent): Boolean {
        when (event.key) {
            256 -> {
                if (displayValue != "0" || pendingOp != null || expressionLine.isNotEmpty()) {
                    handleButton("AC")
                    playClickSound()
                } else {
                    return super.keyPressed(event)
                }
                return true
            }
            257, 335 -> { handleButton("="); playClickSound(); return true }
            259      -> { handleButton("⌫"); playClickSound(); return true }
            in 320..329 -> { handleButton((event.key - 320).toString()); playClickSound(); return true }
            331 -> { handleButton("+"); playClickSound(); return true }
            333 -> { handleButton("−"); playClickSound(); return true }
            332 -> { handleButton("×"); playClickSound(); return true }
            334 -> { handleButton("÷"); playClickSound(); return true }
            330 -> { handleButton("."); playClickSound(); return true }
        }
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val c = Character.toChars(event.codepoint).firstOrNull() ?: return false
        val mapped = when (c) {
            in '0'..'9' -> c.toString()
            '.'         -> "."
            '+'         -> "+"
            '-'         -> "−"
            '*', 'x', 'X' -> "×"
            '/'         -> "÷"
            '='         -> "="
            '\r', '\n'  -> "="
            'k', 'K'    -> "Thousand"
            'm', 'M'    -> "Million"
            'b', 'B'    -> "Billion"
            else        -> null
        }
        if (mapped != null) {
            handleButton(mapped)
            playClickSound()
            return true
        }
        return false
    }

    // comptua handle that input

    private fun handleButton(label: String) {
        when (label) {
            "AC" -> {
                displayValue = "0"; expressionLine = ""
                pendingOp = null; pendingLeft = 0.0; freshInput = true
            }
            "⌫" -> {
                if (!freshInput && displayValue.length > 1)
                    displayValue = displayValue.dropLast(1)
                else {
                    displayValue = "0"; freshInput = true
                }
            }
            "." -> {
                if (freshInput) { displayValue = "0."; freshInput = false }
                else if (!displayValue.contains('.')) displayValue += "."
            }
            "=" -> {
                val op = pendingOp ?: return
                val right = displayValue.toDoubleOrNull() ?: return
                expressionLine = "$pendingLeft $op $right"
                displayValue = compute(pendingLeft, right, op).format()
                pendingOp = null; freshInput = true
            }
            "Thousand" -> {
                val current = displayValue.toDoubleOrNull() ?: 1.0
                val base = if (freshInput || current == 0.0) 1.0 else current
                displayValue = (base * 1_000.0).format()
                expressionLine = ""
                freshInput = true
            }
            "Million" -> {
                val current = displayValue.toDoubleOrNull() ?: 1.0
                val base = if (freshInput || current == 0.0) 1.0 else current
                displayValue = (base * 1_000_000.0).format()
                expressionLine = ""
                freshInput = true
            }
            "Billion" -> {
                val current = displayValue.toDoubleOrNull() ?: 1.0
                val base = if (freshInput || current == 0.0) 1.0 else current
                displayValue = (base * 1_000_000_000.0).format()
                expressionLine = ""
                freshInput = true
            }
            in listOf("+", "−", "×", "÷") -> {
                pendingLeft = displayValue.toDoubleOrNull() ?: 0.0
                pendingOp = label[0]
                expressionLine = "$pendingLeft $label"
                freshInput = true
            }
            else -> {
                if (freshInput) { displayValue = label; freshInput = false }
                else if (displayValue.length < MAX_DIGITS) displayValue += label
            }
        }
    }

    private fun compute(a: Double, b: Double, op: Char): Double = when (op) {
        '+' -> a + b
        '−' -> a - b
        '×' -> a * b
        '÷' -> if (b == 0.0) Double.NaN else a / b
        else -> b
    }

    private fun Double.format(): String {
        if (isNaN()) return "Error"
        val long = toLong()
        return if (this == long.toDouble()) long.toString()
        else "%.10g".format(this).trimEnd('0').trimEnd('.')
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        private const val PANEL_WIDTH  = 220
        private const val PANEL_HEIGHT = 210
        private const val PADDING      = 10
        private const val DISPLAY_H    = 38
        private const val GRID_GAP     = 6
        private const val BTN_GAP      = 4
        private const val BTN_H        = 18
        private const val MAX_DIGITS   = 14

        private const val PANEL_COLOR   = 0x1E1E22
        private const val PANEL_OPACITY = 210
        private const val BG_DIM        = 0x99000000.toInt()
        private const val DISPLAY_BG    = 0x55000000
        private const val BTN_BG        = 0x332a2a32
        private const val BTN_HOVER     = 0x553a3a44
        private const val ACCENT_COLOR  = 0xFF9075D4.toInt()
        private const val ACCENT_HOVER  = 0xFFD0A6FF.toInt()
        private const val NAME_COLOR    = 0xFFFFFFFF.toInt()
        private const val DESC_COLOR    = 0xFF888888.toInt()
    }
}