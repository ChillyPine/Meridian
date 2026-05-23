package io.github.meridian.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundEvents
import net.minecraft.client.resources.sounds.SimpleSoundInstance

data class Category(val name: String, val id: String)

class CategoryPanel(
    private val x: Int,      // left edge of the panel
    private val y: Int,      // top edge of the panel
    private val width: Int,  // should be LEFT_PANEL_WIDTH - BAR_WIDTH so entries don't overlap the divider
    private val height: Int  // should match PANEL_HEIGHT
) {
    companion object {
        private const val ITEM_HEIGHT = 18         // height of each category row
        private const val ITEM_PADDING_X = 8       // left padding for category label text
        private const val ITEM_TOP = 30            // below the divider bar
        private const val HOVER_COLOR = 0x33FFFFFF
        private const val SELECTED_COLOR = 0x55BB86FC
        private const val TEXT_COLOR = 0xFFFFFFFF.toInt()
        private const val TEXT_COLOR_SELECTED = 0xFFBB86FC.toInt()
    }

    // list of all categories shown in the left panel
    val categories = listOf(
        Category("1", "1"),
        Category("2", "2"),
        Category("3", "3"),
        Category("4", "4")
    )

    // tracks which category is currently active; defaults to the first one
    var selected: String = categories.first().id

    // draws each category row, applying hover/selected highlight and appropriate text color
    fun render(guiGraphics: GuiGraphics, font: Font, mouseX: Int, mouseY: Int) {
        categories.forEachIndexed { index, category ->
            val itemY = y + ITEM_TOP + (index * ITEM_HEIGHT)
            val isSelected = category.id == selected
            val isHovered = mouseX in x..(x + width) && mouseY in itemY..(itemY + ITEM_HEIGHT)

            // draw background highlight — selected takes priority over hovered
            when {
                isSelected -> guiGraphics.fill(x, itemY, x + width - 3, itemY + ITEM_HEIGHT, SELECTED_COLOR)
                isHovered -> guiGraphics.fill(x, itemY, x + width - 3, itemY + ITEM_HEIGHT, HOVER_COLOR)
            }

            // draw label with color based on selection state
            val color = if (isSelected) TEXT_COLOR_SELECTED else TEXT_COLOR
            guiGraphics.drawString(font, category.name, x + ITEM_PADDING_X, itemY + 5, color, false)
        }
    }

    // checks if a click lands on any category row; updates selected and returns true if consumed
    fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        categories.forEachIndexed { index, category ->
            val itemY = y + ITEM_TOP + (index * ITEM_HEIGHT)
            if (mouseX in x until (x + width) && mouseY in itemY until (itemY + ITEM_HEIGHT)) {
                selected = category.id
                // play ui click sound, pitch 1.5f to match the snappier feel of the original CT sound
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.5f)
                )
                return true
            }
        }
        return false
    }
}