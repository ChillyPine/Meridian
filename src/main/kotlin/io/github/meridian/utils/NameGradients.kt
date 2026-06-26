package io.github.meridian.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import kotlin.math.roundToInt

object NameGradients {
    private val GRADIENTS: Map<String, List<Int>> = mapOf(
        // 0x1F4FA8, 0x2BD4C4 | 0x0BADF5 | 0x9bafd9, 0x103783
        "ChillyPine" to listOf(0x6274e7, 0x133a94),
        "DwnInFraggleRock" to listOf(0x6A0DAD, 0x9B30FF, 0xC77DFF, 0xE0AAFF),
    )

    fun init() {
        ClientReceiveMessageEvents.MODIFY_GAME.register { message, _ ->
            applyGradients(message)
        }
    }

    @JvmStatic
    fun applyToNameTag(name: Component): Component = applyGradients(name)

    @JvmStatic
    fun applyToTabName(name: Component): Component = applyGradients(name)

    fun applyGradients(message: Component): Component {
        val full = message.string
        if (GRADIENTS.keys.none { full.contains(it) }) return message

        val out = Component.empty()

        for (run in message.toFlatList(Style.EMPTY)) {
            val text = run.string
            val style = run.style
            val buf = StringBuilder()
            fun flush() {
                if (buf.isNotEmpty()) {
                    out.append(Component.literal(buf.toString()).setStyle(style))
                    buf.setLength(0)
                }
            }
            var i = 0
            while (i < text.length) {
                val match = GRADIENTS.entries.firstOrNull { (ign, _) ->
                    text.regionMatches(i, ign, 0, ign.length) && isWholeWord(text, i, ign.length)
                }
                if (match != null) {
                    flush()
                    out.append(gradient(match.key, match.value))
                    i += match.key.length
                } else {
                    buf.append(text[i])
                    i++
                }
            }
            flush()
        }
        return out
    }

    private fun isWholeWord(text: String, start: Int, len: Int): Boolean {
        val end = start + len
        val prevOk = start == 0 || !text[start - 1].isNameChar()
        val nextOk = end >= text.length || !text[end].isNameChar()
        return prevOk && nextOk
    }

    private fun Char.isNameChar(): Boolean = isLetterOrDigit() || this == '_'

    private fun gradient(name: String, stops: List<Int>): Component {
        val out = Component.empty()
        val last = name.length - 1
        for (i in name.indices) {
            val t = if (last <= 0) 0f else i.toFloat() / last
            out.append(Component.literal(name[i].toString()).setStyle(Style.EMPTY.withColor(sample(stops, t))))
        }
        return out
    }

    private fun sample(stops: List<Int>, t: Float): Int {
        if (stops.size == 1) return stops[0]
        val segments = stops.size - 1
        val scaled = (t * segments).coerceIn(0f, segments.toFloat())
        val index = scaled.toInt().coerceAtMost(segments - 1)
        return lerp(stops[index], stops[index + 1], scaled - index)
    }

    private fun lerp(a: Int, b: Int, t: Float): Int {
        val r = channel(a, 16, b, t)
        val g = channel(a, 8, b, t)
        val bl = channel(a, 0, b, t)
        return (r shl 16) or (g shl 8) or bl
    }

    private fun channel(a: Int, shift: Int, b: Int, t: Float): Int {
        val from = (a ushr shift) and 0xFF
        val to = (b ushr shift) and 0xFF
        return (from + (to - from) * t).roundToInt().coerceIn(0, 255)
    }
}