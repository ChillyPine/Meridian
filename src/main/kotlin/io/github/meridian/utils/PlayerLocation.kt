package io.github.meridian.utils

import io.github.meridian.Meridian.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel

// TODO: Make this scoreboard based. This is more reliable and protects against edge cases where people are reconnected.
object DungeonState {
    @Volatile var inDungeon = false
        private set

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text == "Starting in 1 second.") inDungeon = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inDungeon = false
            }
        })
    }
}

object F4State {
    @Volatile var inF4Boss = false
        private set

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!")) inF4Boss = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inF4Boss = false
            }
        })
    }
}

// Could also check the wool on the ceiling. It always starts out red. Or maybe some other blocks?
object F5State {
    @Volatile var inF5Boss = false
        private set

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text.startsWith("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) inF5Boss = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inF5Boss = false
            }
        })
    }
}

object P2State {
    @Volatile var inP2 = false
        private set

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            when (text) {
                "[BOSS] Storm: Pathetic Maxor, just like expected." -> inP2 = true
                "[BOSS] Storm: I should have known that I stood no chance." -> inP2 = false
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inP2 = false
            }
        })
    }
}

// Add another way to detect if in P5 (check for wither king?? maybe that's retarded)
object P5State {
    @Volatile var inP5 = false
        private set

    private var lastLevel: ClientLevel? = null

    fun init() {
        onChatMessage { text, _, _ ->
            if (text == "[BOSS] Wither King: Ohhh?" || text == "[BOSS] Wither King: You... again?") inP5 = true
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inP5 = false
            }
        })
    }
}