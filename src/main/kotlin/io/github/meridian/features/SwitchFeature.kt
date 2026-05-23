package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.drawRoundedRect
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

open class SwitchFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    subcategory: String = "",
    defaultEnabled: Boolean = false
) : Feature(name, description, category, configKey, subcategory) {

    var enabled: Boolean = defaultEnabled

    fun toggle() {
        enabled = !enabled
        FeatureManager.save()
    }

    // Hit-test bounds, set during render. (Stored on the instance so a click can
    // reuse the last layout without recomputing.)
    private var switchX = 0
    private var switchY = 0

    override fun render(
        guiGraphics: GuiGraphics,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int {
        drawRoundedRect(guiGraphics, x, y, width, ROW_HEIGHT, ROW_BG_COLOR, ROW_CORNER_RADIUS)

        guiGraphics.drawString(font, name, x + ROW_PADDING_X, y + ROW_PADDING_Y, NAME_COLOR, false)
        guiGraphics.drawString(
            font, description,
            x + ROW_PADDING_X, y + ROW_PADDING_Y + font.lineHeight + 2,
            DESC_COLOR, false
        )

        switchX = x + width - SWITCH_WIDTH - SWITCH_RIGHT_PADDING
        switchY = y + (ROW_HEIGHT - SWITCH_HEIGHT) / 2
        renderSwitch(guiGraphics)

        return ROW_HEIGHT
    }

    private fun renderSwitch(g: GuiGraphics) {
        val bgColor = if (enabled) SWITCH_ON_BG else SWITCH_OFF_BG
        drawRoundedRect(g, switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, bgColor, SWITCH_CORNER_RADIUS)

        val ballX = if (enabled) {
            switchX + SWITCH_WIDTH - SWITCH_BALL_SIZE - SWITCH_BALL_PADDING
        } else {
            switchX + SWITCH_BALL_PADDING
        }
        val ballY = switchY + (SWITCH_HEIGHT - SWITCH_BALL_SIZE) / 2
        drawRoundedRect(g, ballX, ballY, SWITCH_BALL_SIZE, SWITCH_BALL_SIZE, SWITCH_BALL_COLOR, SWITCH_CORNER_RADIUS)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        if (mouseX in switchX until (switchX + SWITCH_WIDTH) &&
            mouseY in switchY until (switchY + SWITCH_HEIGHT)
        ) {
            toggle()
            playClickSound()
            return true
        }
        return false
    }

    override fun saveTo(json: JsonObject) {
        json.addProperty("enabled", enabled)
    }

    override fun loadFrom(json: JsonObject) {
        if (json.has("enabled")) {
            enabled = json.get("enabled").asBoolean
        }
    }

    companion object {
        private const val ROW_HEIGHT = 38
        private const val ROW_PADDING_X = 8
        private const val ROW_PADDING_Y = 6
        private const val ROW_CORNER_RADIUS = 3
        private const val ROW_BG_COLOR = 0x66000000.toInt()  // ~40% black, layered over the panel

        private const val NAME_COLOR = 0xFFFFFFFF.toInt()
        private const val DESC_COLOR = 0xFFAAAAAA.toInt()

        private const val SWITCH_WIDTH = 28
        private const val SWITCH_HEIGHT = 14
        private const val SWITCH_CORNER_RADIUS = 3    // shared by outer rect and inner square
        private const val SWITCH_RIGHT_PADDING = 8
        private const val SWITCH_OFF_BG = 0xFF555555.toInt()
        private const val SWITCH_ON_BG = 0xFFBB86FC.toInt()
        private const val SWITCH_BALL_COLOR = 0xFFFFFFFF.toInt()
        private const val SWITCH_BALL_SIZE = 10
        private const val SWITCH_BALL_PADDING = 2
    }
}
