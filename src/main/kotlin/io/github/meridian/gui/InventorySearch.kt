package io.github.meridian.gui

import io.github.meridian.Meridian.mc
import io.github.meridian.utils.ItemSearch
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.inventory.Slot

// Always-on search bar for container screens; not a Feature, so there is no
// toggle and nothing is persisted. Slots that don't match the query are dimmed.
//
// The bar is drawn inside a pose scaled to TARGET_SCALE physical px per local
// unit, so it keeps the same on-screen size at every GUI Scale setting. All of
// its layout and hit-testing works in that local space — gui-space mouse coords
// must be divided by scale() before they reach the widget.
object InventorySearch {

    private const val TARGET_SCALE = 3f
    private const val BAR_W = 180
    private const val BOTTOM_MARGIN = 8
    private const val DIM_COLOR = 0xC8101010.toInt()

    private val bar = SearchBar("Search items", maxLength = 128)

    private var parsed: List<ItemSearch.Term> = emptyList()
    private var parsedFrom: String? = null

    private fun scale() = TARGET_SCALE / mc.window.guiScale.toFloat().coerceAtLeast(1f)

    private fun barX(s: Float) = ((mc.window.guiScaledWidth / s).toInt() - BAR_W) / 2
    private fun barY(s: Float) = (mc.window.guiScaledHeight / s).toInt() - SearchBar.HEIGHT - BOTTOM_MARGIN

    // The query persists between screens; only the focus resets on open.
    fun onScreenOpen() = bar.unfocus()

    private fun terms(): List<ItemSearch.Term> {
        if (parsedFrom != bar.query) {
            parsedFrom = bar.query
            parsed = ItemSearch.parse(bar.query)
        }
        return parsed
    }

    fun render(g: GuiGraphicsExtractor, slots: List<Slot>, leftPos: Int, topPos: Int) {
        val terms = terms()
        for (slot in slots) {
            if (!slot.isActive || ItemSearch.matches(slot.item, terms)) continue
            val x = leftPos + slot.x
            val y = topPos + slot.y
            g.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, DIM_COLOR)
        }

        val s = scale()
        val pose = g.pose()
        pose.pushMatrix()
        pose.scale(s, s)
        bar.render(g, mc.font, barX(s), barY(s), BAR_W, SearchBar.HEIGHT)
        pose.popMatrix()
    }

    fun mouseClicked(event: MouseButtonEvent): Boolean {
        val s = scale()
        return bar.mouseClicked((event.x() / s).toInt(), (event.y() / s).toInt(), SearchBar.HEIGHT)
    }

    // While focused every key is swallowed, otherwise typing a letter bound to
    // a hotkey (`e` closes the inventory, 1-9 swap hotbar slots) would fire it
    // instead of entering text. Escape unfocuses rather than clearing, so the
    // query survives and a second Escape closes the screen as usual.
    fun keyPressed(event: KeyEvent): Boolean {
        if (!bar.focused) return false
        if (event.key == KEY_ESCAPE) bar.unfocus() else bar.keyPressed(event)
        return true
    }

    fun charTyped(event: CharacterEvent) = bar.charTyped(event)

    private const val SLOT_SIZE = 16
    private const val KEY_ESCAPE = 256
}
