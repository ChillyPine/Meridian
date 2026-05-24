package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.ColorPicker
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

// A feature row whose value is an ARGB color. Clicking the swatch/button opens
// a ColorPicker scoped to this instance; confirming saves, going back discards.
open class ColorFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    subcategory: String = "",
    dependsOn: Feature? = null,
    defaultColor: Int = 0xFFBB86FC.toInt()
) : Feature(name, description, category, configKey, subcategory, dependsOn) {

    var color: Int = defaultColor

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

        val label = "#%08X".format(color)
        val labelWidth = font.width(label)
        buttonWidth = labelWidth + 2 * BUTTON_INNER_PADDING_X + SWATCH_SIZE + SWATCH_GAP
        buttonX = x + width - buttonWidth - BUTTON_RIGHT_PADDING
        buttonY = y + (ROW_HEIGHT - BUTTON_HEIGHT) / 2

        guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + BUTTON_HEIGHT, BUTTON_BG)

        val swatchX = buttonX + BUTTON_INNER_PADDING_X
        val swatchY = buttonY + (BUTTON_HEIGHT - SWATCH_SIZE) / 2
        // Checkerboard backdrop so transparency is visible without distorting hue.
        drawCheckerboard(guiGraphics, swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 2)
        guiGraphics.fill(swatchX, swatchY, swatchX + SWATCH_SIZE, swatchY + SWATCH_SIZE, color)

        val labelX = swatchX + SWATCH_SIZE + SWATCH_GAP
        val labelY = buttonY + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1
        guiGraphics.drawString(font, label, labelX, labelY, BUTTON_TEXT_COLOR, false)

        return ROW_HEIGHT
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        if (mouseX in buttonX until (buttonX + buttonWidth) &&
            mouseY in buttonY until (buttonY + BUTTON_HEIGHT)
        ) {
            playClickSound()
            val mc = Minecraft.getInstance()
            val parent = mc.screen
            mc.execute {
                mc.setScreen(ColorPicker(color, parent) { newColor ->
                    color = newColor
                    FeatureManager.save()
                })
            }
            return true
        }
        return false
    }

    override fun saveTo(json: JsonObject) {
        json.addProperty("color", color)
    }

    override fun loadFrom(json: JsonObject) {
        if (json.has("color")) {
            color = json.get("color").asInt
        }
    }

    private fun drawCheckerboard(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int, cell: Int) {
        for (col in 0 until w step cell) {
            for (row in 0 until h step cell) {
                val light = ((col / cell + row / cell) % 2 == 0)
                g.fill(x + col, y + row,
                    minOf(x + col + cell, x + w),
                    minOf(y + row + cell, y + h),
                    if (light) CHECKER_LIGHT else CHECKER_DARK)
            }
        }
    }

    companion object {
        private const val BUTTON_HEIGHT = 14
        private const val BUTTON_RIGHT_PADDING = 8
        private const val BUTTON_INNER_PADDING_X = 6
        private const val BUTTON_BG = 0xFF333339.toInt()
        private const val BUTTON_TEXT_COLOR = 0xFFFFFFFF.toInt()
        private const val SWATCH_SIZE = 8
        private const val SWATCH_GAP = 6
        private const val CHECKER_LIGHT = 0xFFCCCCCC.toInt()
        private const val CHECKER_DARK = 0xFF999999.toInt()
    }
}