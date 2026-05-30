package io.github.meridian.hud

import com.google.gson.JsonObject
import io.github.meridian.Meridian
import io.github.meridian.Meridian.mc
import io.github.meridian.gui.HudEditScreen
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement as FabricHudElement

// Central registry + renderer for movable/scalable HUD overlays.
//
// GUI-scale independence: vanilla draws the whole HUD inside a transform scaled
// by the player's "GUI Scale" video setting, so anything drawn naively grows and
// shrinks with that setting. We don't want that for HUD elements — their size
// should depend only on each element's own `scale`. To achieve this every draw
// counter-scales the pose by 1/guiScale, which puts the element into a
// physical-pixel coordinate space. The element is then positioned and sized in
// physical pixels, so changing the GUI Scale setting leaves it untouched.
//
// Two coordinate spaces are in play:
//   - physical px  : where elements are rendered/positioned (gui-scale-independent)
//   - gui-scaled px : the space mouse events and the Screen use
// `lastX/Y/W/H` on each element are stored in gui-scaled px so the editor can
// hit-test directly against mouse coordinates.
object HudManager {
    private val elements = mutableListOf<HudElement>()

    // Idempotent — feature `object` singletons may run their init more than once
    // across class reloads in dev.
    fun register(element: HudElement) {
        if (elements.none { it.id == element.id }) elements += element
    }

    fun elements(): List<HudElement> = elements.toList()

    // Registers the live HUD layer. Drawn last so it sits above vanilla HUD.
    fun init() {
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(Meridian.MOD_ID, "hud"),
            FabricHudElement { g, _ -> renderLive(g) },
        )
    }

    private fun renderLive(g: GuiGraphics) {
        if (mc.options.hideGui) return
        if (mc.level == null) return
        // The editor renders its own (preview) copies — don't double-draw.
        if (mc.screen is HudEditScreen) return
        val font = mc.font
        for (el in elements) {
            val lines = el.content()
            if (lines.isEmpty()) continue
            draw(g, font, el, lines)
        }
    }

    // Shared draw used by both the live layer and the editor. Applies the
    // element's anchor + scale in a gui-scale-independent space, paints the
    // lines, and records the on-screen (gui-scaled) bounds on the element.
    fun draw(g: GuiGraphics, font: Font, el: HudElement, lines: List<String>) {
        val guiScale = mc.window.guiScale.toFloat().coerceAtLeast(1f)
        val swP = mc.window.guiScaledWidth * guiScale
        val shP = mc.window.guiScaledHeight * guiScale

        val w = contentWidth(font, lines).toFloat()
        val h = contentHeight(font, lines).toFloat()
        val wP = w * el.scale
        val hP = h * el.scale

        // Clamp so the element stays fully on-screen.
        val pxP = (el.anchorX * swP).coerceIn(0f, (swP - wP).coerceAtLeast(0f))
        val pyP = (el.anchorY * shP).coerceIn(0f, (shP - hP).coerceAtLeast(0f))

        // Bounds back in gui-scaled px for the editor's mouse hit-testing.
        el.lastX = (pxP / guiScale).toInt()
        el.lastY = (pyP / guiScale).toInt()
        el.lastW = (wP / guiScale).toInt()
        el.lastH = (hP / guiScale).toInt()

        val pose = g.pose()
        pose.pushMatrix()
        pose.scale(1f / guiScale, 1f / guiScale) // local units are now physical px
        pose.translate(pxP, pyP)
        pose.scale(el.scale, el.scale)
        var ly = 0
        for (line in lines) {
            g.drawString(font, line, 0, ly, el.color(), el.shadow)
            ly += font.lineHeight + HudElement.LINE_GAP
        }
        pose.popMatrix()
    }

    fun contentWidth(font: Font, lines: List<String>): Int =
        lines.maxOfOrNull { font.width(it) } ?: 0

    fun contentHeight(font: Font, lines: List<String>): Int =
        if (lines.isEmpty()) 0
        else lines.size * font.lineHeight + (lines.size - 1) * HudElement.LINE_GAP

    // ---- Persistence ("hud" section in config.json) ----------------------

    fun saveTo(json: JsonObject) {
        for (el in elements) {
            val o = JsonObject()
            o.addProperty("x", el.anchorX)
            o.addProperty("y", el.anchorY)
            o.addProperty("scale", el.scale)
            json.add(el.id, o)
        }
    }

    fun loadFrom(json: JsonObject) {
        for (el in elements) {
            val o = json.getAsJsonObject(el.id) ?: continue
            if (o.has("x")) el.anchorX = o.get("x").asFloat.coerceIn(0f, 1f)
            if (o.has("y")) el.anchorY = o.get("y").asFloat.coerceIn(0f, 1f)
            if (o.has("scale")) {
                el.scale = o.get("scale").asFloat.coerceIn(HudElement.MIN_SCALE, HudElement.MAX_SCALE)
            }
        }
    }
}
