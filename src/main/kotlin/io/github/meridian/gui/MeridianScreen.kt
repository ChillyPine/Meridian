package io.github.meridian.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MeridianScreen : Screen(Component.literal("Meridian")) {

    companion object {
        private const val PANEL_WIDTH = 400            // main panel
        private const val PANEL_HEIGHT = 250
        private const val LEFT_PANEL_WIDTH = 100       // overlay panel for category buttons

        private const val PANEL_COLOR = 0x1E1E22       // RGB only (no alpha byte)
        private const val PANEL_OPACITY = 200          // 0 = invisible, 255 = fully opaque

        private const val TITLE_TEXT = "Meridian"
        private const val TITLE_COLOR = 0xFFFFFFFF.toInt()
        private const val TITLE_TOP_PADDING = 8        // distance from panel top to text
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Intentionally empty: this override disables the default background blur.
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        val x = (width - PANEL_WIDTH) / 2
        val y = (height - PANEL_HEIGHT) / 2
        val color = (PANEL_OPACITY shl 24) or PANEL_COLOR

        // Main panel
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, color)
        // Left overlay panel — sits on top of the main panel's left portion
        guiGraphics.fill(x, y, x + LEFT_PANEL_WIDTH, y + PANEL_HEIGHT, color)

        // Title centered over the main panel's full width
        val textX = x + (PANEL_WIDTH - font.width(TITLE_TEXT)) / 2
        val textY = y + TITLE_TOP_PADDING
        guiGraphics.drawString(font, TITLE_TEXT, textX, textY, TITLE_COLOR, false)
    }

    override fun isPauseScreen(): Boolean = false
}