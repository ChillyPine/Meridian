package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.ACCENT_COLOR
import io.github.meridian.gui.HOVER_COLOR
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

open class DropdownFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    val options: List<String>,
    subcategory: String = "",
    dependsOn: Feature? = null,
    defaultIndex: Int = 0,
    // Optional. Called when the user clicks an option (not on programmatic loads —
    // behavior code that needs to react every time should read selectedOption directly).
    val onChange: (String) -> Unit = {}
) : Feature(name, description, category, configKey, subcategory, dependsOn) {

    var selectedIndex: Int = defaultIndex.coerceIn(0, options.lastIndex)
        private set

    val selectedOption: String get() = options[selectedIndex]

    private var expanded = false

    private var buttonX = 0
    private var buttonY = 0
    private var buttonWidth = 0

    private var dropdownX = 0
    private var dropdownY = 0
    private var dropdownW = 0
    private var dropdownH = 0

    override fun render(
        guiGraphics: GuiGraphics,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int {
        val rowHeight = drawHeader(guiGraphics, font, x, y, width)

        buttonWidth = controlBoxWidth(font)
        buttonX = x + width - buttonWidth - BUTTON_RIGHT_PADDING
        buttonY = y + (rowHeight - BUTTON_HEIGHT) / 2

        guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + BUTTON_HEIGHT, ACCENT_COLOR)

        val labelY = buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1
        val maxLabelWidth = buttonWidth - INNER_PAD_X * 2 - ARROW_ZONE
        val label = font.plainSubstrByWidth(selectedOption, maxLabelWidth)
        guiGraphics.drawString(font, label, buttonX + INNER_PAD_X, labelY, TEXT_COLOR, false)

        val arrow = if (expanded) "▲" else "▼"
        val arrowX = buttonX + buttonWidth - ARROW_ZONE + (ARROW_ZONE - font.width(arrow)) / 2
        guiGraphics.drawString(font, arrow, arrowX, labelY, TEXT_COLOR, false)

        return rowHeight
    }

    override fun hasOpenOverlay(): Boolean = expanded

    override fun renderOverlay(guiGraphics: GuiGraphics, font: Font, mouseX: Int, mouseY: Int) {
        if (!expanded) return

        dropdownW = buttonWidth
        dropdownH = options.size * ITEM_HEIGHT + DROPDOWN_PAD_Y * 2
        dropdownX = buttonX
        dropdownY = buttonY + BUTTON_HEIGHT + 1

        guiGraphics.fill(dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + dropdownH, DROPDOWN_BG_COLOR)
        guiGraphics.fill(dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + 1, ACCENT_COLOR)

        options.forEachIndexed { i, option ->
            val itemY = dropdownY + DROPDOWN_PAD_Y + i * ITEM_HEIGHT
            val isHovered = mouseX in dropdownX until (dropdownX + dropdownW) &&
                    mouseY in itemY until (itemY + ITEM_HEIGHT)
            when {
                i == selectedIndex -> guiGraphics.fill(dropdownX, itemY, dropdownX + dropdownW, itemY + ITEM_HEIGHT, ITEM_SELECTED_COLOR)
                isHovered          -> guiGraphics.fill(dropdownX, itemY, dropdownX + dropdownW, itemY + ITEM_HEIGHT, HOVER_COLOR)
            }
            val labelY = itemY + (ITEM_HEIGHT - font.lineHeight) / 2 + 1
            guiGraphics.drawString(font, option, dropdownX + INNER_PAD_X, labelY, TEXT_COLOR, false)
        }
    }

    override fun controlBoxWidth(font: Font): Int {
        val widest = options.maxOfOrNull { font.width(it) } ?: 0
        return widest + INNER_PAD_X * 2 + ARROW_ZONE + BUTTON_RIGHT_PADDING
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        if (expanded &&
            mouseX in dropdownX until (dropdownX + dropdownW) &&
            mouseY in dropdownY until (dropdownY + dropdownH)
        ) {
            val i = (mouseY - dropdownY - DROPDOWN_PAD_Y) / ITEM_HEIGHT
            if (i in options.indices) {
                selectedIndex = i
                expanded = false
                onChange(options[i])
                playClickSound()
            }
            return true
        }

        if (mouseX in buttonX until (buttonX + buttonWidth) &&
            mouseY in buttonY until (buttonY + BUTTON_HEIGHT)
        ) {
            expanded = !expanded
            playClickSound()
            return true
        }

        if (expanded) {
            expanded = false
        }

        return false
    }

    // Persist by option NAME, not by index, so reordering the options list later
    // doesn't silently shift everyone's saved choice. Falls back to defaultIndex
    // if the saved name is no longer in the options list (e.g. option renamed/removed).
    override fun saveTo(json: JsonObject) {
        json.addProperty("selected", selectedOption)
    }

    override fun loadFrom(json: JsonObject) {
        val saved = json.get("selected")?.asString ?: return
        val idx = options.indexOf(saved)
        if (idx >= 0) selectedIndex = idx
    }

    companion object {
        private const val BUTTON_HEIGHT = 14
        private const val BUTTON_RIGHT_PADDING = 8
        private const val INNER_PAD_X = 6
        private const val ARROW_ZONE = 14
        private const val TEXT_COLOR = 0xFFFFFFFF.toInt()

        private const val DROPDOWN_PAD_Y = 2
        private const val ITEM_HEIGHT = 16
        private const val DROPDOWN_BG_COLOR = 0xCC1E1E22.toInt()
        private const val ITEM_SELECTED_COLOR = 0x33BB86FC
    }
}