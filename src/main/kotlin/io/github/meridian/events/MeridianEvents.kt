package io.github.meridian.events

import io.github.meridian.utils.Toggleable
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.network.chat.Component
import java.util.concurrent.CopyOnWriteArrayList

/** An incoming game chat message: [text] has color codes stripped; [component] is the original. */
data class ChatMessage(val text: String, val component: Component, val overlay: Boolean)

private val COLOR_CODES = Regex("§.")

class ListenerBus<T> {
    private val listeners = CopyOnWriteArrayList<(T) -> Unit>()

    fun add(cb: (T) -> Unit) {
        listeners.addIfAbsent(cb)
    }

    fun remove(cb: (T) -> Unit) {
        listeners.remove(cb)
    }

    fun fire(arg: T) {
        for (l in listeners) l(arg)
    }
}

class BusListener<T>(
    private val bus: ListenerBus<T>,
    private val cb: (T) -> Unit
) : Toggleable() {
    override fun add() = bus.add(cb)
    override fun remove() = bus.remove(cb)
}

object MeridianEvents {
    val render = ListenerBus<LevelRenderContext>()
    val tick = ListenerBus<Unit>()
    val chat = ListenerBus<ChatMessage>()

    fun init() {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register { ctx -> render.fire(ctx) }
        ClientTickEvents.END_CLIENT_TICK.register { tick.fire(Unit) }
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            chat.fire(ChatMessage(message.string.replace(COLOR_CODES, ""), message, overlay))
        }
    }
}
