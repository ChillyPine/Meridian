package io.github.meridian.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import kotlin.math.roundToInt

object NameGradients {
    /** Color stops plus any formatting to stack on top of the gradient. */
    private class Gradient(
        val stops: List<Int>,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underlined: Boolean = false,
    )

    private val GRADIENTS: Map<String, Gradient> = mapOf(
        // 0x1F4FA8, 0x2BD4C4 | 0x0BADF5 | 0x9bafd9, 0x103783
        "ChillyPine" to Gradient(listOf(0x6274e7, 0x133a94), bold = true),
        "DwnInFraggleRock" to Gradient(listOf(0x6A0DAD, 0x9B30FF, 0xC77DFF, 0xE0AAFF)),
    )

    // Standard legacy colors only — SkyHanni's Compact Tab List (and likely other
    // tab-list mods) round-trips names through formattedTextCompat()/removeColor(),
    // which only knows how to strip §-legacy codes. Raw RGB TextColor serializes to
    // a hex-tag format their regex doesn't recognize, so it leaks through as literal
    // text and pollutes their parsed player name. Snap to the nearest legacy color
    // for anything rendered into the tab list.
    private val LEGACY_COLORS: List<ChatFormatting> = ChatFormatting.entries.filter { it.color != null }

    // Excludes white/gray/black — used for saturated stops so a light pastel tint
    // (e.g. 0xE0AAFF) doesn't get pulled toward WHITE just because it's pale; it stays
    // in the same hue family (e.g. LIGHT_PURPLE) instead.
    private val CHROMATIC_LEGACY: List<ChatFormatting> = LEGACY_COLORS.filterNot {
        it == ChatFormatting.WHITE || it == ChatFormatting.GRAY ||
                it == ChatFormatting.DARK_GRAY || it == ChatFormatting.BLACK
    }

    fun init() {
        ClientReceiveMessageEvents.MODIFY_GAME.register { message, _ ->
            applyGradients(message, forTabList = false)
        }
    }

    @JvmStatic
    fun applyToNameTag(name: Component): Component = applyGradients(name, forTabList = false)

    @JvmStatic
    fun applyToTabName(name: Component): Component = applyGradients(name, forTabList = true)

    fun applyGradients(message: Component, forTabList: Boolean): Component {
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
                    out.append(gradient(match.key, match.value, style, forTabList))
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

    private fun gradient(name: String, g: Gradient, sourceStyle: Style, forTabList: Boolean): Component {
        val out = Component.empty()
        // Preserve the source run's style (hover/click events, font, etc.) instead of
        // resetting to Style.EMPTY — only the flags the gradient itself controls, plus
        // color, are overridden.
        val base = sourceStyle
            .withBold(g.bold)
            .withItalic(g.italic)
            .withUnderlined(g.underlined)
        val last = name.length - 1
        for (i in name.indices) {
            val t = if (last <= 0) 0f else i.toFloat() / last
            val rgb = sample(g.stops, t)
            val color = if (forTabList) {
                nearestLegacy(rgb).color!!
            } else {
                rgb
            }
            out.append(Component.literal(name[i].toString()).setStyle(base.withColor(color)))
        }
        return out
    }

    private fun nearestLegacy(rgb: Int): ChatFormatting {
        val candidates = if (saturationOf(rgb) > 0.15f) CHROMATIC_LEGACY else LEGACY_COLORS
        return candidates.minBy { colorDistance(it.color!!, rgb) }
    }

    private fun saturationOf(rgb: Int): Float {
        val r = ((rgb shr 16) and 0xFF) / 255f
        val g = ((rgb shr 8) and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        return if (max <= 0f) 0f else (max - min) / max
    }

    private fun colorDistance(a: Int, b: Int): Int {
        val dr = ((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)
        val dg = ((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)
        val db = (a and 0xFF) - (b and 0xFF)
        return dr * dr + dg * dg + db * db
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