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