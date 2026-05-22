package io.github.meridian.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MeridianScreen : Screen(Component.literal("Meridian")) {

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderTransparentBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun isPauseScreen(): Boolean = false
}
