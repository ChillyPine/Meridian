package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.ACCENT_COLOR
import io.github.meridian.utils.playClickSound
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

open class SwitchFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    subcategory: String = "",
    dependsOn: Feature? = null,
    defaultEnabled: Boolean = false
) : Feature(name, description, category, configKey, subcategory, dependsOn) {

    var enabled: Boolean = defaultEnabled

    override fun isDependencyActive(): Boolean = enabled

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
        val rowHeight = drawHeader(guiGraphics, font, x, y, width)

        switchX = x + width - SWITCH_WIDTH - SWITCH_RIGHT_PADDING
        switchY = y + (rowHeight - SWITCH_HEIGHT) / 2
        renderSwitch(guiGraphics)

        return rowHeight
    }

    override fun controlBoxWidth(font: Font): Int = SWITCH_WIDTH + SWITCH_RIGHT_PADDING

    private fun renderSwitch(g: GuiGraphics) {
        val bgColor = if (enabled) ACCENT_COLOR else SWITCH_OFF_BG
        g.fill(switchX, switchY, switchX + SWITCH_WIDTH, switchY + SWITCH_HEIGHT, bgColor)

        val ballX = if (enabled) {
            switchX + SWITCH_WIDTH - SWITCH_BALL_SIZE - SWITCH_BALL_PADDING
        } else {
            switchX + SWITCH_BALL_PADDING
        }
        val ballY = switchY + (SWITCH_HEIGHT - SWITCH_BALL_SIZE) / 2
        g.fill(ballX, ballY, ballX + SWITCH_BALL_SIZE, ballY + SWITCH_BALL_SIZE, SWITCH_BALL_COLOR)
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
        private const val SWITCH_WIDTH = 28
        private const val SWITCH_HEIGHT = 14
        private const val SWITCH_RIGHT_PADDING = 8
        private const val SWITCH_OFF_BG = 0xFF555555.toInt()
        private const val SWITCH_BALL_COLOR = 0xFFFFFFFF.toInt()
        private const val SWITCH_BALL_SIZE = 10
        private const val SWITCH_BALL_PADDING = 2
    }
}
