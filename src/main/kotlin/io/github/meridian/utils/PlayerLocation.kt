package io.github.meridian.utils

import io.github.meridian.Meridian.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel

// Each state exposes a reactive [state]: features bind their listeners to it (via the `gate`
// param on SwitchFeature.onRender/onTick/onChat) so they physically detach when out of the
// relevant phase, instead of polling the flag every frame. [inX] stays as a plain read for
// always-on trackers and non-feature callers.

// Derived from [SkyblockLocation], so it survives reconnects and mid-run joins — the old
// "Starting in 1 second." chat sniff missed both. The scoreboard is OR'd in because it repopulates
// within a few ticks of the world loading while /locraw takes about a second to answer; erring
// toward "in dungeon" is the harmless direction for every consumer (waypoints, chat blockers).
object DungeonState {
    private const val CATACOMBS = "The Catacombs"

    val state: State<Boolean> =
        SkyblockLocation.islandState.zip(SkyblockLocation.areaState) { island, area ->
            island == Island.DUNGEON || area.startsWith(CATACOMBS)
        }

    val inDungeon: Boolean get() = state.value
}

object F4State {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inF4Boss: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!")) _state.value = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                _state.value = false
            }
        })
    }
}

// Could also check the wool on the ceiling. It always starts out red. Or maybe some other blocks?
object F5State {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inF5Boss: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) _state.value = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                _state.value = false
            }
        })
    }
}

object F6State {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inF6Boss: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!")) _state.value = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                _state.value = false
            }
        })
    }
}

enum class BossPhase(val label: String) {
    P1("P1"), P2("P2"), P3("P3"), P4("P4"), P5("P5")
}

object BossState {
    private data class PhaseTriggers(val start: String, val end: String?)

    private val triggers = mapOf(
        BossPhase.P1 to PhaseTriggers(
            start = "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!",
            end = "[BOSS] Maxor: I'M TOO YOUNG TO DIE AGAIN!"
        ),
        BossPhase.P2 to PhaseTriggers(
            start = "[BOSS] Storm: Pathetic Maxor, just like expected.",
            end = "[BOSS] Storm: I should have known that I stood no chance."
        ),
        BossPhase.P3 to PhaseTriggers(
            start = "[BOSS] Goldor: Who dares trespass into my domain?",
            end = "[BOSS] Necron: You went further than any human before, congratulations."
        ),
        BossPhase.P4 to PhaseTriggers(
            start = "[BOSS] Necron: You went further than any human before, congratulations.",
            end = "[BOSS] Necron: All this, for nothing..."
        ),
        BossPhase.P5 to PhaseTriggers(
            start = "[BOSS] Necron: All this, for nothing...",
            end = null
        ),
    )

    private val states = BossPhase.entries.associateWith { BasicState(false) }
    private var lastLevel: ClientLevel? = null

    fun state(phase: BossPhase): State<Boolean> = states.getValue(phase)
    operator fun get(phase: BossPhase): Boolean = states.getValue(phase).value

    // use these vaules, Example : BossState.inP2 will return true when in P2
    val inP1: Boolean get() = get(BossPhase.P1) // unused rn
    val inP2: Boolean get() = get(BossPhase.P2)
    val inP3: Boolean get() = get(BossPhase.P3)
    val inP4: Boolean get() = get(BossPhase.P4) // unused rn
    val inP5: Boolean get() = get(BossPhase.P5) // unused rn

    fun init() {
        onChatMessage { text, _, _ ->
            for ((phase, t) in triggers) {
                when (text) {
                    t.start -> states.getValue(phase).value = true
                    t.end -> states.getValue(phase).value = false
                }
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                states.values.forEach { it.value = false }
            }
        })
    }
}
