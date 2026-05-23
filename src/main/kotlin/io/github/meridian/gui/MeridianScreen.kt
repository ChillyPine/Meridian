package io.github.meridian.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class MeridianScreen : Screen(Component.literal("Meridian")) {

    companion object {
        private const val PANEL_WIDTH = 400            // main panel
        private const val PANEL_HEIGHT = 250
        private const val LEFT_PANEL_WIDTH = 100       // overlay panel for category buttons
        private const val BAR_WIDTH = 3
        private const val BAR_COLOR = 0xFFBB86FC.toInt()

        private const val VERSION_TEXT = "v1.0.0"
        private const val VERSION_COLOR = 0xFFBB86FC.toInt()

        private const val PANEL_COLOR = 0x1E1E22       // RGB only (no alpha byte)
        private const val PANEL_OPACITY = 200          // 0 = invisible, 255 = fully opaque

        private const val TITLE_TEXT = "Meridian"
        private const val TITLE_COLOR = 0xFFFFFFFF.toInt()
        private const val TITLE_TOP_PADDING = 8        // distance from panel top to text
    }

    private lateinit var categoryPanel: CategoryPanel

    override fun init() {
        super.init()
        val x = (width - PANEL_WIDTH) / 2
        val y = (height - PANEL_HEIGHT) / 2
        categoryPanel = CategoryPanel(x, y, LEFT_PANEL_WIDTH - BAR_WIDTH, PANEL_HEIGHT)
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
        // Colored bar
        guiGraphics.fill(x + LEFT_PANEL_WIDTH - BAR_WIDTH, y, x + LEFT_PANEL_WIDTH, y + PANEL_HEIGHT, BAR_COLOR)
        // Dividing bar for categories/title
        guiGraphics.fill(x + 5, y + 22, x + (LEFT_PANEL_WIDTH - 8), y + 22 + (BAR_WIDTH - 2), BAR_COLOR)

        // Title centered over the main panel's full width
        val textX = x + (LEFT_PANEL_WIDTH - font.width(TITLE_TEXT)) / 2
        val textY = y + TITLE_TOP_PADDING
        guiGraphics.drawString(font, TITLE_TEXT, textX, textY, TITLE_COLOR, false)


        // Version number — pinned to bottom-left of left panel
        val versionTextX = x + 5
        val versionTextY = y + PANEL_HEIGHT - font.lineHeight - 5
        guiGraphics.drawString(font, VERSION_TEXT, versionTextX, versionTextY, VERSION_COLOR, false)

        // Category panel
        categoryPanel.render(guiGraphics, font, mouseX, mouseY)

        // Right panel content
        when (categoryPanel.selected) {
            "1"   -> renderCombatSettings(guiGraphics, x, y)
            "2" -> renderMovementSettings(guiGraphics, x, y)
            "3"   -> renderRenderSettings(guiGraphics, x, y)
            "4"     -> renderMiscSettings(guiGraphics, x, y)
        }
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        if (categoryPanel.mouseClicked(mouseButtonEvent.x.toInt(), mouseButtonEvent.y.toInt())) return true
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        return super.mouseReleased(mouseButtonEvent)

    }

    override fun isPauseScreen(): Boolean = false

    // catagories

    private fun renderCombatSettings(guiGraphics: GuiGraphics, panelX: Int, panelY: Int) {
        val contentX = panelX + LEFT_PANEL_WIDTH + 8
        val contentY = panelY + 10
        guiGraphics.drawString(font, "100000", contentX, contentY, 0xFFBB86FC.toInt(), false)
    }

    private fun renderMovementSettings(guiGraphics: GuiGraphics, panelX: Int, panelY: Int) {
        val contentX = panelX + LEFT_PANEL_WIDTH + 8
        val contentY = panelY + 10
        guiGraphics.drawString(font, "2", contentX, contentY, 0xFFBB86FC.toInt(), false)
    }

    private fun renderRenderSettings(guiGraphics: GuiGraphics, panelX: Int, panelY: Int) {
        val contentX = panelX + LEFT_PANEL_WIDTH + 8
        val contentY = panelY + 10
        guiGraphics.drawString(font, "3", contentX, contentY, 0xFFBB86FC.toInt(), false)
    }

    private fun renderMiscSettings(guiGraphics: GuiGraphics, panelX: Int, panelY: Int) {
        val contentX = panelX + LEFT_PANEL_WIDTH + 8
        val contentY = panelY + 10
        guiGraphics.drawString(font, "4", contentX, contentY, 0xFFBB86FC.toInt(), false)
    }
}