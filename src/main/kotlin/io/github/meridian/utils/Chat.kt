package io.github.meridian.utils

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.client.gui.components.ChatComponent
import io.github.meridian.Meridian.mc

// Sends a chat message in chat
fun sendChatMessage(message: Any) {
    mc.execute { mc.player?.connection?.sendChat(message.toString()) }
}

fun sendCommand(command: String) {
    mc.execute { mc.player?.connection?.sendCommand(command) }
}

fun sendClientMessage(message: Component) {
    mc.execute { mc.gui.chat.addClientSystemMessage(message) }
}

fun sendClientMessage(message: String) {
    mc.execute { mc.gui.chat.addClientSystemMessage(Component.literal(message)) }
}

// Simulates a game message as if the server sent it — runs the full receive
// pipeline (ALLOW_GAME, then GAME) and shows the message in chat unless a
// listener vetoes it. Lets chat blockers be tested without a real server.
fun simulateGameMessage(message: Component) {
    mc.execute {
        val allowed = ClientReceiveMessageEvents.ALLOW_GAME.invoker()
            .allowReceiveGameMessage(message, false)
        if (!allowed) return@execute
        ClientReceiveMessageEvents.GAME.invoker().onReceiveGameMessage(message, false)
        mc.gui.chat.addClientSystemMessage(message)
    }
}

fun simulateGameMessage(message: String) {
    simulateGameMessage(Component.literal(message))
}

// Used for just plain strings. Anything passed to this function will end up being a string. Should not be used for anything where formatting is required.
fun modMessage(message: Any?, prefix: String = "§6Meridian §5»§r ", chatStyle: Style? = null) {
    val text = Component.literal("$prefix$message")
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.execute { mc.gui.chat.addClientSystemMessage(text) }
}

// Used for rich messages, meaning hover-able text, clickable links, etc
fun modMessage(message: Component, prefix: String = "§6Meridian §5»§r ", chatStyle: Style? = null) {
    val text = Component.literal(prefix).append(message)
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.execute { mc.gui.chat.addClientSystemMessage(text) }
}

//fun devMessage(message: Any?) {
//    if (!ClickGUIModule.devMessage) return
//    modMessage(message, "§3Odin§bDev §8»§r ")
//}

// Registers a listener for incoming game chat messages. The callback receives
// the plain text (color codes stripped), the original Component, and whether
// the message was an action-bar overlay. Return value is ignored — use
// ClientReceiveMessageEvents.ALLOW_GAME directly if you need to veto.
fun onChatMessage(
    includeOverlay: Boolean = false,
    callback: (text: String, component: Component, overlay: Boolean) -> Unit,
) {
    ClientReceiveMessageEvents.GAME.register { message, overlay ->
        if (overlay && !includeOverlay) return@register
        callback(message.string.replace(Regex("§."), ""), message, overlay)
    }
}

fun getChatBreak(): String {
    return ChatComponent.getWidth(mc.options.chatWidth().get()).let {
        "§9§m" + "-".repeat(it / mc.font.width("-"))
    }
}
