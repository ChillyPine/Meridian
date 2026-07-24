package io.github.meridian.features.impl.vanilla

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.mixin.accessor.ChatComponentAccessor
import kotlin.math.floor

object ChatEraser : SwitchFeature(
    name = "Chat Eraser",
    description = "Press Backspace while hovering over a chat message to remove it from the chat window.\nDoes not affect Minecraft logs.",
    category = "Vanilla",
    configKey = "chat_eraser",
    subcategory = "Miscellaneous"
) {
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
