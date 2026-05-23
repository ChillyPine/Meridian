package io.github.meridian.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MeridianScreen : Screen(Component.literal("Meridian")) {

    companion object {
        private const val PANEL_WIDTH = 400
        private const val PANEL_HEIGHT = 250
        private const val PANEL_COLOR = 0x1E1E22       // RGB only (no alpha byte)
        private const val PANEL_OPACITY = 200          // 0 = invisible, 255 = fully opaque
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Intentionally empty: this override disables the default background blur.
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        val x = (width - PANEL_WIDTH) / 2
        val y = (height - PANEL_HEIGHT) / 2
        val color = (PANEL_OPACITY shl 24) or PANEL_COLOR
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, color)
    }

    override fun isPauseScreen(): Boolean = false
}