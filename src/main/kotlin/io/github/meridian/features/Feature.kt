package io.github.meridian.features

import com.google.gson.JsonObject
import io.github.meridian.gui.DESC_COLOR
import io.github.meridian.gui.DESC_RIGHT_GAP
import io.github.meridian.gui.NAME_COLOR
import io.github.meridian.gui.ROW_BG_COLOR
import io.github.meridian.gui.ROW_HEIGHT
import io.github.meridian.gui.ROW_PADDING_X
import io.github.meridian.gui.ROW_PADDING_Y
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

abstract class Feature(
    val name: String,
    val description: String,
    val category: String,        // matches CategoryPanel selected ids
    val configKey: String,       // persisted key in config.json — keep stable
    val subcategory: String = "", // optional grouping inside a category
    val dependsOn: Feature? = null
) {
    private var visibilityCondition: (() -> Boolean)? = null

    protected fun showWhen(condition: () -> Boolean) {
        visibilityCondition = condition
    }

    fun isVisible(): Boolean {
        val parent = dependsOn ?: return true
        if (!parent.isVisible()) return false
        return visibilityCondition?.invoke() ?: parent.isDependencyActive()
    }

    fun isActive(): Boolean = isVisible() && isDependencyActive()

    open fun isDependencyActive(): Boolean = true

    fun depth(): Int {
        var d = 0
        var p = dependsOn
        while (p != null) { d++; p = p.dependsOn }
        return d
    }

    abstract fun render(
        guiGraphics: GuiGraphicsExtractor,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int

    abstract fun mouseClicked(mouseX: Int, mouseY: Int): Boolean

    abstract fun saveTo(json: JsonObject)

    abstract fun loadFrom(json: JsonObject)

    open fun keyPressed(event: KeyEvent): Boolean = false
    open fun charTyped(event: CharacterEvent): Boolean = false

    open fun renderOverlay(guiGraphics: GuiGraphicsExtractor, font: Font, mouseX: Int, mouseY: Int) {}

    open fun hasOpenOverlay(): Boolean = false

    protected abstract fun controlBoxWidth(font: Font): Int

    private fun descriptionMaxWidth(font: Font, rowWidth: Int): Int =
        (rowWidth - ROW_PADDING_X - controlBoxWidth(font) - DESC_RIGHT_GAP).coerceAtLeast(1)

    fun measureRowHeight(font: Font, rowWidth: Int): Int {
        val lines = font.split(Component.literal(description), descriptionMaxWidth(font, rowWidth)).size
        val contentH = ROW_PADDING_Y + font.lineHeight + 2 + lines * font.lineHeight + ROW_PADDING_Y
        return maxOf(ROW_HEIGHT, contentH)
    }

    protected fun drawHeader(g: GuiGraphicsExtractor, font: Font, x: Int, y: Int, width: Int): Int {
        val descMaxW = descriptionMaxWidth(font, width)
        val lines = font.split(Component.literal(description), descMaxW)
        val contentH = ROW_PADDING_Y + font.lineHeight + 2 + lines.size * font.lineHeight + ROW_PADDING_Y
        val rowHeight = maxOf(ROW_HEIGHT, contentH)

        g.fill(x, y, x + width, y + rowHeight, ROW_BG_COLOR)
        g.text(font, name, x + ROW_PADDING_X, y + ROW_PADDING_Y, NAME_COLOR, false)
        var dy = y + ROW_PADDING_Y + font.lineHeight + 2
        for (line in lines) {
            g.text(font, line, x + ROW_PADDING_X, dy, DESC_COLOR, false)
            dy += font.lineHeight
        }
        return rowHeight
    }
}
