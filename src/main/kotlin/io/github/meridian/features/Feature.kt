package io.github.meridian.features

import com.google.gson.JsonObject
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

abstract class Feature(
    val name: String,
    val description: String,
    val category: String,        // matches CategoryPanel selected ids
    val configKey: String,       // persisted key in config.json — keep stable
    val subcategory: String = "" // optional grouping inside a category
) {
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
}
