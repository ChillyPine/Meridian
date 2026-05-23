package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.DESC_COLOR
import io.github.meridian.gui.NAME_COLOR
import io.github.meridian.gui.ROW_BG_COLOR
import io.github.meridian.gui.ROW_HEIGHT
import io.github.meridian.gui.ROW_PADDING_X
import io.github.meridian.gui.ROW_PADDING_Y
import io.github.meridian.gui.ACCENT_COLOR
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

// A feature row with a clickable action button on the right. The `onClick` lambda
// runs whatever the caller wants — send a command, open a URL, reset config,
// trigger any side effect. No persistent state.
open class ButtonFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    val buttonLabel: String,
    val onClick: () -> Unit,
    subcategory: String = ""
) : Feature(name, description, category, configKey, subcategory) {

    // Hit-test bounds, set during render.
    private var buttonX = 0
    private var buttonY = 0
    private var buttonWidth = 0

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

        // Button sized to its label so any string fits cleanly.
        val labelWidth = font.width(buttonLabel)
        buttonWidth = labelWidth + 2 * BUTTON_INNER_PADDING_X
        buttonX = x + width - buttonWidth - BUTTON_RIGHT_PADDING
        buttonY = y + (ROW_HEIGHT - BUTTON_HEIGHT) / 2

        guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + BUTTON_HEIGHT, ACCENT_COLOR)

        val labelX = buttonX + (buttonWidth - labelWidth) / 2
        val labelY = buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1
        guiGraphics.drawString(font, buttonLabel, labelX, labelY, BUTTON_TEXT_COLOR, false)

        return ROW_HEIGHT
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        if (mouseX in buttonX until (buttonX + buttonWidth) &&
            mouseY in buttonY until (buttonY + BUTTON_HEIGHT)
        ) {
            onClick()
            playClickSound()
            return true
        }
        return false
    }

    // No persistent state — empty objects skipped by FeatureManager.save().
    override fun saveTo(json: JsonObject) {}
    override fun loadFrom(json: JsonObject) {}

    companion object {
        private const val BUTTON_HEIGHT = 14
        private const val BUTTON_RIGHT_PADDING = 8
        private const val BUTTON_INNER_PADDING_X = 8
        private const val BUTTON_TEXT_COLOR = 0xFFFFFFFF.toInt()
    }
}
