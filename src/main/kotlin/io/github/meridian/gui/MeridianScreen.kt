package io.github.meridian.gui

import io.github.meridian.features.FeatureManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.fabricmc.loader.api.FabricLoader

class MeridianScreen : Screen(Component.literal("Meridian")) {

    companion object {
        private const val PANEL_WIDTH = 400            // main panel
        private const val PANEL_HEIGHT = 250
        private const val LEFT_PANEL_WIDTH = 100       // overlay panel for category buttons
        private const val BAR_WIDTH = 3
        private const val BAR_COLOR = 0xFFBB86FC.toInt()

        private val VERSION_TEXT = "v" + FabricLoader.getInstance()
            .getModContainer("meridian")
            .map { it.metadata.version.friendlyString }
            .orElse("?.?.?")
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

        // Right panel content — features in the currently selected category
        renderFeaturesForCategory(guiGraphics, x, y, categoryPanel.selected, mouseX, mouseY)
    }

    private fun renderFeaturesForCategory(
        g: GuiGraphics,
        panelX: Int,
        panelY: Int,
        category: String,
        mouseX: Int,
        mouseY: Int
    ) {
        val contentX = panelX + LEFT_PANEL_WIDTH + 8
        val contentY = panelY + 10
        val contentWidth = PANEL_WIDTH - LEFT_PANEL_WIDTH - 16

        val features = FeatureManager.byCategory(category)
        val grouped = features.groupBy { it.subcategory }

        var currentY = contentY
        for ((subcat, feats) in grouped) {
            if (subcat.isNotEmpty()) {
                g.drawString(font, subcat, contentX + (contentWidth - font.width(subcat)) / 2, currentY, BAR_COLOR, false)
                currentY += font.lineHeight + 4
            }
            for (feat in feats) {
                val rowHeight = feat.render(g, font, contentX, currentY, contentWidth, mouseX, mouseY)
                currentY += rowHeight + 4
            }
        }
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = mouseButtonEvent.x.toInt()
        val my = mouseButtonEvent.y.toInt()
        if (categoryPanel.mouseClicked(mx, my)) return true
        for (feat in FeatureManager.byCategory(categoryPanel.selected)) {
            if (feat.mouseClicked(mx, my)) return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        for (feat in FeatureManager.byCategory(categoryPanel.selected)) {
            if (feat.keyPressed(event)) return true
        }
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        for (feat in FeatureManager.byCategory(categoryPanel.selected)) {
            if (feat.charTyped(event)) return true
        }
        return super.charTyped(event)
    }

    override fun isPauseScreen(): Boolean = false
}