package io.github.meridian.features.types

import com.google.gson.JsonObject
import io.github.meridian.events.BusListener
import io.github.meridian.events.ChatMessage
import io.github.meridian.events.MeridianEvents
import io.github.meridian.features.Feature
import io.github.meridian.features.FeatureManager
import io.github.meridian.gui.ACCENT_COLOR
import io.github.meridian.utils.BasicState
import io.github.meridian.utils.ChatBlockListener
import io.github.meridian.utils.ChatBlocker
import io.github.meridian.utils.State
import io.github.meridian.utils.Toggleable
import io.github.meridian.utils.playClickSound
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

open class SwitchFeature(
    name: String,
    description: String,
    category: String,
    configKey: String,
    subcategory: String = "",
    dependsOn: Feature? = null,
    defaultEnabled: Boolean = false
) : Feature(name, description, category, configKey, subcategory, dependsOn) {

    /** Reactive backing for [enabled]; every write fans out to bound listeners. */
    val enabledState = BasicState(defaultEnabled)

    var enabled: Boolean
        get() = enabledState.value
        set(value) { enabledState.value = value }

    /** Own enabled-state AND-ed with the `dependsOn` chain (the parent already folds its own chain). */
    val activeState: State<Boolean> =
        (dependsOn as? SwitchFeature)?.let { enabledState.zip(it.activeState, Boolean::and) } ?: enabledState

    private val children = mutableListOf<Toggleable>()

    init {
        activeState.listen { active -> if (active) onActivate() else onDeactivate() }
    }

    /** The state a listener attaches on: active, optionally AND-ed with a phase/area [gate]. */
    private fun effective(gate: State<Boolean>?): State<Boolean> =
        if (gate == null) activeState else activeState.zip(gate, Boolean::and)

    /**
     * Registers a render callback attached only while this feature is active. If a [gate] state
     * is given (e.g. a phase state from PlayerLocation), the listener also detaches whenever the
     * gate is false — so an enabled-but-out-of-phase feature has no listener on the bus at all.
     */
    protected fun onRender(gate: State<Boolean>? = null, cb: (ctx: LevelRenderContext) -> Unit) {
        children += BusListener(MeridianEvents.render, cb).bind(effective(gate))
    }

    /** Registers an end-client-tick callback, attached only while active (and the [gate], if given). */
    protected fun onTick(gate: State<Boolean>? = null, cb: () -> Unit) {
        children += BusListener<Unit>(MeridianEvents.tick) { cb() }.bind(effective(gate))
    }

    /** Registers an incoming-game-chat callback, attached only while active (and the [gate], if given). */
    protected fun onChat(
        gate: State<Boolean>? = null,
        includeOverlay: Boolean = false,
        cb: (text: String, component: Component, overlay: Boolean) -> Unit
    ) {
        val listener = BusListener(MeridianEvents.chat) { m: ChatMessage ->
            if (!m.overlay || includeOverlay) cb(m.text, m.component, m.overlay)
        }
        children += listener.bind(effective(gate))
    }

    private fun addBlockRule(gate: State<Boolean>?, rule: ChatBlocker.Rule) {
        children += ChatBlockListener(rule).bind(effective(gate))
    }

    /** Blocks incoming game messages matching [pattern], while active (and the [gate], if given). */
    protected fun blockChat(pattern: Regex, gate: State<Boolean>? = null) =
        addBlockRule(gate) { pattern.containsMatchIn(it.string) }

    /** Blocks incoming game messages containing [substring], while active (and the [gate], if given). */
    protected fun blockChat(substring: String, gate: State<Boolean>? = null) =
        addBlockRule(gate) { it.string.contains(substring) }

    /** Blocks incoming game messages whose plain text matches [predicate], while active (and the [gate], if given). */
    protected fun blockChatIf(gate: State<Boolean>? = null, predicate: (String) -> Boolean) =
        addBlockRule(gate) { predicate(it.string) }

    /** Blocks (or transforms) incoming game messages via a raw [Component] rule, while active (and the [gate], if given). */
    protected fun blockChatRaw(gate: State<Boolean>? = null, rule: (Component) -> Boolean) =
        addBlockRule(gate) { rule(it) }

    /** Called when the feature becomes active (enabled + dependencies satisfied). */
    open fun onActivate() {}

    /** Called when the feature becomes inactive — use to clear transient state (HUD data, caches). */
    open fun onDeactivate() {}

    override fun isDependencyActive(): Boolean = enabled

    fun toggle() {
        enabled = !enabled
        FeatureManager.save()
    }

    private var switchX = 0
    private var switchY = 0

    override fun render(
        guiGraphics: GuiGraphicsExtractor,
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int
    ): Int {
        val rowHeight = drawHeader(guiGraphics, font, x, y, width)

        switchX = x + width - SWITCH_WIDTH - SWITCH_RIGHT_PADDING
        switchY = y + (rowHeight - SWITCH_HEIGHT) / 2
        renderSwitch(guiGraphics)

        return rowHeight
    }

    override fun controlBoxWidth(font: Font): Int = SWITCH_WIDTH + SWITCH_RIGHT_PADDING

    private fun renderSwitch(g: GuiGraphicsExtractor) {
        val bgColor = if (enabled) ACCENT_COLOR else SWITCH_OFF_BG
        g.fill(switchX, switchY, switchX + SWITCH_WIDTH, switchY + SWITCH_HEIGHT, bgColor)

        val ballX = if (enabled) {
            switchX + SWITCH_WIDTH - SWITCH_BALL_SIZE - SWITCH_BALL_PADDING
        } else {
            switchX + SWITCH_BALL_PADDING
        }
        val ballY = switchY + (SWITCH_HEIGHT - SWITCH_BALL_SIZE) / 2
        g.fill(ballX, ballY, ballX + SWITCH_BALL_SIZE, ballY + SWITCH_BALL_SIZE, SWITCH_BALL_COLOR)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int): Boolean {
        if (mouseX in switchX until (switchX + SWITCH_WIDTH) &&
            mouseY in switchY until (switchY + SWITCH_HEIGHT)
        ) {
            toggle()
            playClickSound()
            return true
        }
        return false
    }

    override fun saveTo(json: JsonObject) {
        json.addProperty("enabled", enabled)
    }

    override fun loadFrom(json: JsonObject) {
        if (json.has("enabled")) {
            enabled = json.get("enabled").asBoolean
        }
    }

    companion object {
        private const val SWITCH_WIDTH = 28
        private const val SWITCH_HEIGHT = 14
        private const val SWITCH_RIGHT_PADDING = 8
        private const val SWITCH_OFF_BG = 0xFF555555.toInt()
        private const val SWITCH_BALL_COLOR = 0xFFFFFFFF.toInt()
        private const val SWITCH_BALL_SIZE = 10
        private const val SWITCH_BALL_PADDING = 2
    }
}