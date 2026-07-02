package io.github.meridian.utils

import io.github.meridian.Meridian.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel

// Each state exposes a reactive [state]: features bind their listeners to it (via the `gate`
// param on SwitchFeature.onRender/onTick/onChat) so they physically detach when out of the
// relevant phase, instead of polling the flag every frame. [inX] stays as a plain read for
// always-on trackers and non-feature callers.

// TODO: Make this scoreboard based. This is more reliable and protects against edge cases where people are reconnected.
object DungeonState {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inDungeon: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text == "Starting in 1 second.") _state.value = true
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

object P2State {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inP2: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            when (text) {
                "[BOSS] Storm: Pathetic Maxor, just like expected." -> _state.value = true
                "[BOSS] Storm: I should have known that I stood no chance." -> _state.value = false
            }
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

// Add another way to detect if in P5 (check for wither king?? maybe that's retarded)
object P5State {
    private val _state = BasicState(false)
    val state: State<Boolean> = _state
    val inP5: Boolean get() = _state.value

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text == "[BOSS] Necron: All this, for nothing...") _state.value = true
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
