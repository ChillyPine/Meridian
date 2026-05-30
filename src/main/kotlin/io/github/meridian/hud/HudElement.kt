package io.github.meridian.hud

// A draggable, scalable on-screen overlay owned by a feature.
//
// Content is expressed as a list of pre-formatted lines — legacy §-color codes
// are honored by Font/GuiGraphics, so each line carries its own coloring. An
// empty live content() list means "nothing to draw right now": the element
// stays invisible in-game but is still positionable in the HUD editor through
// its preview() content.
//
// Position is stored as a fraction [0,1] of the gui-scaled screen so the element
// keeps its relative spot across resolutions and gui-scale changes. The actual
// pixel placement (and final on-screen bounds, used by the editor for
// hit-testing) is computed by HudManager during each draw.
abstract class HudElement(
    // Stable persistence key — never rename.
    val id: String,
    // Shown in the editor's hover tooltip — conventionally the owning feature's name.
    val name: String,
    private val defaultAnchorX: Float = 0.5f,
    private val defaultAnchorY: Float = 0.1f,
) {
    // Top-left anchor as a fraction [0,1] of the gui-scaled screen.
    var anchorX: Float = defaultAnchorX
    var anchorY: Float = defaultAnchorY
    var scale: Float = 1f

    // Screen-space bounds (gui-scaled px) from the most recent draw. Written by
    // HudManager.draw; read by the editor for hit-testing, outlines and tooltips.
    var lastX: Int = 0
    var lastY: Int = 0
    var lastW: Int = 0
    var lastH: Int = 0

    // Live content (real data). Empty list => not drawn in-game.
    abstract fun content(): List<String>

    // Editor preview content. Defaults to live content, falling back to the
    // element name so there is always something to grab. Override for a richer
    // example (e.g. sample values when the feature has no live data).
    open fun preview(): List<String> = content().ifEmpty { listOf(name) }

    fun resetToDefault() {
        anchorX = defaultAnchorX
        anchorY = defaultAnchorY
        scale = 1f
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 4f
        const val LINE_GAP = 1 // unscaled px between stacked lines
    }
}
