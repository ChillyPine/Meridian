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