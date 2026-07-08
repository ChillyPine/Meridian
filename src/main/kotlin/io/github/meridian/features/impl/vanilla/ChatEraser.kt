package io.github.meridian.features.impl.vanilla

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.mixin.accessor.ChatComponentAccessor
import kotlin.math.floor

object ChatEraser : SwitchFeature(
    name = "Chat Eraser",
    description = "Press Backspace while hovering over a chat message to remove it from the display.\nDoes not remove it from your logs.",
    category = "Vanilla",
    configKey = "chat_eraser",
    subcategory = "Miscellaneous"
) {
    // Mirrors ChatComponent's layout math (extractRenderState): lines stack upward
    // from chatBottom, each `lineHeight` tall, inside a pose scaled by `scale` and
    // translated (4,0). Inverting that transform turns the mouse into the same
    // local space, so the hovered trimmed-line index — and its parent GuiMessage —
    // falls straight out. Dropping the message from allMessages and rebuilding the
    // trimmed buffer leaves the log untouched (already written on receipt).
    fun eraseHoveredMessage(): Boolean {
        val window = mc.window
        val acc = mc.gui.chat as ChatComponentAccessor

        val trimmed = acc.`meridian$getTrimmedMessages`()
        if (trimmed.isEmpty()) return false

        val scale = acc.`meridian$getScale`()
        if (scale <= 0.0) return false

        val lineHeight = (9.0 * (mc.options.chatLineSpacing().get() + 1.0)).toInt()
        if (lineHeight <= 0) return false

        val chatBottom = floor((window.guiScaledHeight - 40) / scale).toInt()
        val maxWidth = (acc.`meridian$getWidth`() / scale).toInt()

        val localX = mc.mouseHandler.getScaledXPos(window) / scale - 4.0
        val localY = mc.mouseHandler.getScaledYPos(window) / scale
        if (localX < -4.0 || localX > maxWidth + 8.0) return false

        val scrollPos = acc.`meridian$getChatScrollbarPos`()
        val visible = minOf(trimmed.size - scrollPos, mc.gui.chat.linesPerPage)
        for (i in 0 until visible) {
            val entryBottom = chatBottom - i * lineHeight
            val entryTop = entryBottom - lineHeight
            if (localY < entryTop || localY >= entryBottom) continue

            val parent = trimmed[i + scrollPos].parent()
            // Zero the scroll before refreshing: refreshTrimmedMessages replays every
            // message through addMessageToDisplayQueue, which bumps the scrollbar once
            // per line while chatting + scrolled — starting from 0 avoids that inflation.
            acc.`meridian$setChatScrollbarPos`(0)
            acc.`meridian$getAllMessages`().removeAll { it === parent }
            acc.`meridian$refreshTrimmedMessages`()
            val maxScroll = maxOf(0, acc.`meridian$getTrimmedMessages`().size - mc.gui.chat.linesPerPage)
            acc.`meridian$setChatScrollbarPos`(minOf(scrollPos, maxScroll))
            return true
        }
        return false
    }
}
