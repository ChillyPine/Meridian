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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

abstract class Feature(
    val name: String,
    val description: String,
    val category: String,        // matches CategoryPanel selected ids
    val configKey: String,       // persisted key in config.json — keep stable
    val subcategory: String = "", // optional grouping inside a category
    // If set, this feature is a child of another: it's only shown and only "active"
    // when every ancestor's gate is satisfied. Reference the parent by object
    // (e.g. dependsOn = TestSwitch) — type-safe and refactor-safe.
    // Only types that override isDependencyActive() (currently SwitchFeature) usefully
    // gate children. Using a non-gating type as a parent is allowed but meaningless —
    // children of it will always be visible.
    val dependsOn: Feature? = null
) {
    // True if every ancestor's gate is currently satisfied.
    fun isVisible(): Boolean {
        val parent = dependsOn ?: return true
        return parent.isDependencyActive() && parent.isVisible()
    }

    // True if this feature would do work right now: visible AND its own gate
    // is satisfied. Behavior hooks (chat listeners, tick callbacks, etc.) should
    // check isActive() rather than reading raw state like SwitchFeature.enabled,
    // so a child stays inert when its parent is off.
    fun isActive(): Boolean = isVisible() && isDependencyActive()

    // Override in types whose own state gates children. Default: ungated (true).
    // SwitchFeature returns `enabled`; a future dropdown would return e.g.
    // `value != defaultValue`.
    open fun isDependencyActive(): Boolean = true

    // Number of ancestors above this feature (0 for top-level). Used by the GUI
    // to indent child rows.
    fun depth(): Int {
        var d = 0
        var p = dependsOn
        while (p != null) { d++; p = p.dependsOn }
        return d
    }

    // Render this feature as a row anchored at (x, y) with the given width.
    // Returns the row's pixel height so the caller can stack the next row beneath.
    abstract fun render(
        guiGraphics: GuiGraphics,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int

    // Returns true if the click was consumed by this feature.
    abstract fun mouseClicked(mouseX: Int, mouseY: Int): Boolean

    // Write this feature's settings into the given json object (mutated in place).
    abstract fun saveTo(json: JsonObject)

    // Apply settings from the given json object (may be empty if not present).
    abstract fun loadFrom(json: JsonObject)

    // Keyboard input forwarded from the parent screen. Default: ignore.
    // Return true if consumed.
    open fun keyPressed(event: KeyEvent): Boolean = false
    open fun charTyped(event: CharacterEvent): Boolean = false

    // Pixel width of the right-side control plus its own right padding. Used to
    // figure out how much horizontal room is left for the wrapped description.
    // Each feature type knows its control geometry; types whose control width
    // depends on the label (button, color) compute it from the font.
    protected abstract fun controlBoxWidth(font: Font): Int

    // Width available for the wrapped description text within a row of the given
    // total width. Description starts at ROW_PADDING_X and must leave DESC_RIGHT_GAP
    // before the control box.
    private fun descriptionMaxWidth(font: Font, rowWidth: Int): Int =
        (rowWidth - ROW_PADDING_X - controlBoxWidth(font) - DESC_RIGHT_GAP).coerceAtLeast(1)

    // Computes the would-be row height for a given row width without drawing.
    // The screen uses this to lay out scrolling correctly when descriptions wrap.
    fun measureRowHeight(font: Font, rowWidth: Int): Int {
        val lines = font.split(Component.literal(description), descriptionMaxWidth(font, rowWidth)).size
        val contentH = ROW_PADDING_Y + font.lineHeight + 2 + lines * font.lineHeight + ROW_PADDING_Y
        return maxOf(ROW_HEIGHT, contentH)
    }

    // Draws the shared row chrome (background, name, wrapped description) and
    // returns the row height. Each render() should call this first, then draw
    // its control vertically centered within the returned height.
    protected fun drawHeader(g: GuiGraphics, font: Font, x: Int, y: Int, width: Int): Int {
        val descMaxW = descriptionMaxWidth(font, width)
        val lines = font.split(Component.literal(description), descMaxW)
        val contentH = ROW_PADDING_Y + font.lineHeight + 2 + lines.size * font.lineHeight + ROW_PADDING_Y
        val rowHeight = maxOf(ROW_HEIGHT, contentH)

        g.fill(x, y, x + width, y + rowHeight, ROW_BG_COLOR)
        g.drawString(font, name, x + ROW_PADDING_X, y + ROW_PADDING_Y, NAME_COLOR, false)
        var dy = y + ROW_PADDING_Y + font.lineHeight + 2
        for (line in lines) {
            g.drawString(font, line, x + ROW_PADDING_X, dy, DESC_COLOR, false)
            dy += font.lineHeight
        }
        return rowHeight
    }
}
