package io.github.meridian.gui

import io.github.meridian.features.Feature
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
        private const val PANEL_WIDTH = 510            // main panel
        private const val PANEL_HEIGHT = 270
        private const val LEFT_PANEL_WIDTH = 100       // overlay panel for category buttons
        private const val BAR_WIDTH = 3
        private const val BAR_COLOR = 0xFFBB86FC.toInt()

        private val VERSION_TEXT = "v" + FabricLoader.getInstance()
            .getModContainer("meridian")
            .map { it.metadata.version.friendlyString }
            .orElse("?.?.?")
        private const val VERSION_COLOR = 0xFFBB86FC.toInt()

        private const val PANEL_COLOR = 0x1E1E22       // RGB only (no alpha byte)
        private const val PANEL_OPACITY = 210          // 0 = invisible, 255 = fully opaque

        private const val TITLE_TEXT = "§lMeridian"
        private const val TITLE_COLOR = 0xFFFFFFFF.toInt()
        private const val TITLE_TOP_PADDING = 8

        private const val CONTENT_TOP_PADDING = 10
        private const val CONTENT_BOTTOM_PADDING = 10
        private const val SCROLLBAR_WIDTH = 4
        private const val SCROLLBAR_PADDING = 4
        private const val SCROLLBAR_TRACK_COLOR = 0x55000000
        private const val SCROLLBAR_THUMB_COLOR = 0xFFBB86FC.toInt()
        private const val SCROLLBAR_THUMB_HOVER = 0xFFD0A6FF.toInt()
        private const val MIN_THUMB_HEIGHT = 16
        private const val WHEEL_STEP_PX = 16

        private const val SEARCH_TOP_GAP = 6
        // Fixed width so the search bar size doesn't grow with the main panel.
        // Sits centered below the panel.
        private const val SEARCH_WIDTH = 266
    }

    private lateinit var categoryPanel: CategoryPanel
    private val searchBar = SearchBar()

    private var searchX = 0
    private var searchY = 0

    // Scroll state for the right-side content area.
    private var scrollOffset = 0
    private var lastTotalContentH = 0
    private var lastViewportH = 0
    private var lastMaxScroll = 0
    private var lastScrollbarShown = false

    // Cached layout for hit-testing (set during render).
    private var contentLeft = 0
    private var contentRight = 0
    private var contentTop = 0
    private var contentBottom = 0
    private var scrollbarTrackX = 0
    private var scrollbarTrackY = 0
    private var scrollbarTrackH = 0
    private var thumbY = 0
    private var thumbH = 0

    private var draggingThumb = false
    private var thumbDragOffsetY = 0

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

        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, color)
        guiGraphics.fill(x, y, x + LEFT_PANEL_WIDTH, y + PANEL_HEIGHT, color)
        guiGraphics.fill(x + LEFT_PANEL_WIDTH - BAR_WIDTH, y, x + LEFT_PANEL_WIDTH, y + PANEL_HEIGHT, BAR_COLOR)
        guiGraphics.fill(x + 5, y + 22, x + (LEFT_PANEL_WIDTH - 8), y + 22 + (BAR_WIDTH - 2), BAR_COLOR)

        val textX = x + (LEFT_PANEL_WIDTH - font.width(TITLE_TEXT)) / 2
        val textY = y + TITLE_TOP_PADDING
        guiGraphics.drawString(font, TITLE_TEXT, textX, textY, TITLE_COLOR, false)

        val versionTextX = x + 5
        val versionTextY = y + PANEL_HEIGHT - font.lineHeight - 5
        guiGraphics.drawString(font, VERSION_TEXT, versionTextX, versionTextY, VERSION_COLOR, false)

        categoryPanel.render(guiGraphics, font, mouseX, mouseY)

        renderFeaturesForCategory(guiGraphics, x, y, categoryPanel.selected, mouseX, mouseY)

        searchX = x + (PANEL_WIDTH - SEARCH_WIDTH) / 2
        searchY = y + PANEL_HEIGHT + SEARCH_TOP_GAP
        searchBar.render(guiGraphics, font, searchX, searchY, SEARCH_WIDTH, SearchBar.HEIGHT)
    }

    private fun renderFeaturesForCategory(
        g: GuiGraphics,
        panelX: Int,
        panelY: Int,
        category: String,
        mouseX: Int,
        mouseY: Int
    ) {
        val rightEdge = panelX + PANEL_WIDTH - 8

        contentLeft = panelX + LEFT_PANEL_WIDTH + 8
        contentTop = panelY + CONTENT_TOP_PADDING
        contentBottom = panelY + PANEL_HEIGHT - CONTENT_BOTTOM_PADDING

        val viewportH = contentBottom - contentTop
        val searching = searchBar.query.isNotEmpty()
        val features = (if (searching) searchResults(searchBar.query)
                        else FeatureManager.byCategory(category))
            .filter { it.isVisible() }
        val grouped = if (searching) features.groupBy {
                          if (it.subcategory.isEmpty()) it.category
                          else "${it.category}→${it.subcategory}"
                      } else features.groupBy { it.subcategory }

        // Row heights are dynamic because descriptions wrap. Measure twice: once
        // assuming no scrollbar (wider rows → fewer wrapped lines), and if that
        // already overflows the viewport, again at the narrower scrollbar width
        // (which can re-wrap descriptions taller and grow totalH further).
        fun measureTotalH(rowsWidth: Int): Int {
            var t = 0
            for ((subcat, feats) in grouped) {
                if (subcat.isNotEmpty()) t += font.lineHeight + 4
                for (f in feats) {
                    val indent = f.depth() * CHILD_INDENT_PX
                    t += f.measureRowHeight(font, rowsWidth - indent) + 4
                }
            }
            if (t > 0) t -= 4 // last gap not visible
            return t
        }

        val widthNoScrollbar = rightEdge - contentLeft
        val totalHNoSb = measureTotalH(widthNoScrollbar)
        val showScrollbar = totalHNoSb > viewportH

        contentRight = if (showScrollbar) rightEdge - SCROLLBAR_WIDTH - SCROLLBAR_PADDING else rightEdge
        val contentW = contentRight - contentLeft
        val totalH = if (showScrollbar) measureTotalH(contentW) else totalHNoSb

        val maxScroll = maxOf(0, totalH - viewportH)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)

        lastTotalContentH = totalH
        lastViewportH = viewportH
        lastMaxScroll = maxScroll
        lastScrollbarShown = showScrollbar

        g.enableScissor(contentLeft, contentTop, contentRight, contentBottom)
        var currentY = contentTop - scrollOffset
        for ((subcat, feats) in grouped) {
            if (subcat.isNotEmpty()) {
                g.drawString(font, subcat, contentLeft + (contentW - font.width(subcat)) / 2, currentY, BAR_COLOR, false)
                currentY += font.lineHeight + 4
            }
            for (feat in feats) {
                val indent = feat.depth() * CHILD_INDENT_PX
                val rowHeight = feat.render(
                    g, font,
                    contentLeft + indent, currentY,
                    contentW - indent,
                    mouseX, mouseY
                )
                currentY += rowHeight + 4
            }
        }
        g.disableScissor()

        // Overlays (dropdown menus, future popups). Drawn after the row scissor is
        // released so they can extend past the row's natural bounds.
        for (feat in features) feat.renderOverlay(g, font, mouseX, mouseY)

        if (showScrollbar) renderScrollbar(g, rightEdge - SCROLLBAR_WIDTH, mouseX, mouseY)
    }

    private fun renderScrollbar(g: GuiGraphics, trackX: Int, mouseX: Int, mouseY: Int) {
        scrollbarTrackX = trackX
        scrollbarTrackY = contentTop
        scrollbarTrackH = lastViewportH

        g.fill(scrollbarTrackX, scrollbarTrackY,
               scrollbarTrackX + SCROLLBAR_WIDTH, scrollbarTrackY + scrollbarTrackH,
               SCROLLBAR_TRACK_COLOR)

        thumbH = ((scrollbarTrackH.toLong() * lastViewportH / lastTotalContentH).toInt())
            .coerceAtLeast(MIN_THUMB_HEIGHT)
            .coerceAtMost(scrollbarTrackH)
        val thumbTravel = scrollbarTrackH - thumbH
        thumbY = if (lastMaxScroll == 0) scrollbarTrackY
                 else scrollbarTrackY + (scrollOffset.toLong() * thumbTravel / lastMaxScroll).toInt()

        val hovering = mouseX in scrollbarTrackX..(scrollbarTrackX + SCROLLBAR_WIDTH) &&
                       mouseY in thumbY..(thumbY + thumbH)
        val thumbColor = if (hovering || draggingThumb) SCROLLBAR_THUMB_HOVER else SCROLLBAR_THUMB_COLOR
        g.fill(scrollbarTrackX, thumbY, scrollbarTrackX + SCROLLBAR_WIDTH, thumbY + thumbH, thumbColor)
    }

    private fun searchResults(query: String): List<Feature> {
        val needle = query.lowercase()
        return FeatureManager.all().filter {
            it.name.lowercase().contains(needle) ||
            it.description.lowercase().contains(needle)
        }
    }

    private fun inContentArea(mx: Int, my: Int) =
        mx in contentLeft until contentRight && my in contentTop until contentBottom

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val mx = mouseButtonEvent.x.toInt()
        val my = mouseButtonEvent.y.toInt()

        if (searchBar.mouseClicked(mx, my, SearchBar.HEIGHT)) return true

        // Any feature with an open overlay (e.g. an expanded dropdown menu) gets
        // first dibs on the click — its overlay may extend past the row scissor,
        // and we want a click on a menu item below contentBottom to land correctly.
        // If the feature returns false, its overlay has already closed itself; we
        // still skip the normal row-routing for this feature so we don't double-handle.
        val visibleInCategory = FeatureManager.byCategory(categoryPanel.selected)
            .filter { it.isVisible() }
        val overlayFeat = visibleInCategory.firstOrNull { it.hasOpenOverlay() }
        if (overlayFeat != null && overlayFeat.mouseClicked(mx, my)) return true

        if (categoryPanel.mouseClicked(mx, my)) return true

        // Scrollbar
        if (lastScrollbarShown &&
            mx in scrollbarTrackX..(scrollbarTrackX + SCROLLBAR_WIDTH) &&
            my in scrollbarTrackY..(scrollbarTrackY + scrollbarTrackH)) {
            if (my in thumbY..(thumbY + thumbH)) {
                draggingThumb = true
                thumbDragOffsetY = my - thumbY
            } else {
                // Click on track outside the thumb: jump-scroll one viewport.
                val direction = if (my < thumbY) -1 else 1
                scrollOffset = (scrollOffset + direction * lastViewportH).coerceIn(0, lastMaxScroll)
            }
            return true
        }

        // Feature rows — gate by viewport so clipped rows aren't clickable.
        // Hidden (parent-gated) features must not be clickable either; their last
        // rendered hit-bounds are stale once the parent goes off. Skip overlayFeat:
        // it's already had its turn above.
        if (inContentArea(mx, my)) {
            for (feat in visibleInCategory) {
                if (feat === overlayFeat) continue
                if (feat.mouseClicked(mx, my)) return true
            }
        } else {
            // Click outside the input area still needs to unfocus any focused TextFeature.
            for (feat in visibleInCategory) {
                if (feat === overlayFeat) continue
                feat.mouseClicked(mx, my)
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        draggingThumb = false
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (draggingThumb && lastMaxScroll > 0) {
            val my = event.y.toInt()
            val newThumbTop = my - thumbDragOffsetY - scrollbarTrackY
            val thumbTravel = (scrollbarTrackH - thumbH).coerceAtLeast(1)
            scrollOffset = (newThumbTop.toLong() * lastMaxScroll / thumbTravel)
                .toInt().coerceIn(0, lastMaxScroll)
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (lastMaxScroll > 0 && inContentArea(mouseX.toInt(), mouseY.toInt())) {
            scrollOffset = (scrollOffset - (scrollY * WHEEL_STEP_PX).toInt())
                .coerceIn(0, lastMaxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (searchBar.keyPressed(event)) {
            scrollOffset = 0
            return true
        }
        for (feat in FeatureManager.byCategory(categoryPanel.selected)) {
            if (feat.isVisible() && feat.keyPressed(event)) return true
        }
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (searchBar.charTyped(event)) {
            scrollOffset = 0
            return true
        }
        for (feat in FeatureManager.byCategory(categoryPanel.selected)) {
            if (feat.isVisible() && feat.charTyped(event)) return true
        }
        return super.charTyped(event)
    }

    override fun isPauseScreen(): Boolean = false
}
