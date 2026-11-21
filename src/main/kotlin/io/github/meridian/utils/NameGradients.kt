package io.github.meridian.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.meridian.Meridian
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object NameGradients {
    // edit this list to add or remove people we want to have custom name colors
    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/ChillyPine/Meridian/main/data/name_colors.json"
    private const val REFRESH_MINUTES = 10L

    /** Color stops plus any formatting to stack on top of the gradient. */
    private class Gradient(
        val stops: List<Int>,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underlined: Boolean = false,
    )

    // force dev names no mater what (even if github is down MAWHAHAHAHA)
    private val BUILT_IN: Map<String, Gradient> = mapOf(
        // 0x1F4FA8, 0x2BD4C4 | 0x0BADF5 | 0x9bafd9, 0x103783
        "ChillyPine" to Gradient(listOf(0x6274e7, 0x133a94), bold = true),
        "DwnInFraggleRock" to Gradient(listOf(0x6A0DAD, 0x9B30FF, 0xC77DFF, 0xE0AAFF)),
    )

    // Read on the render thread, replaced wholesale from the network thread.
    @Volatile private var gradients: Map<String, Gradient> = BUILT_IN
    @Volatile private var remoteCount = 0

    private val VALID_NAME = Regex("^[A-Za-z0-9_]{1,16}$")

    // Errors raised before the player is in a world wait here until they join.
    private val pendingErrors = ConcurrentLinkedQueue<String>()
    @Volatile private var lastErrors: List<String> = emptyList()

    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "Meridian-NameGradients").apply { isDaemon = true }
    }

    fun init() {
        ClientReceiveMessageEvents.MODIFY_GAME.register { message, _ ->
            applyGradients(message)
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            while (true) modMessage(pendingErrors.poll() ?: break)
        }
        executor.scheduleWithFixedDelay(::refresh, 0, REFRESH_MINUTES, TimeUnit.MINUTES)
    }

    // Re-download the remote list now (used by /md namecolors reload).
    fun reload(onDone: (Int) -> Unit = {}) {
        executor.execute {
            lastErrors = emptyList() // a manual reload always shows current errors
            refresh()
            onDone(remoteCount)
        }
    }

    private fun refresh() {
        val errors = ArrayList<String>()
        try {
            val body = get(REMOTE_URL)
            if (body == null) {
                errors += "§cERROR: Couldn't fetch custom name colors.\n§7Try running§a /md namecolors reload.\n§7Submit a bug report at our discord if the issue remains. "
                return
            }
            val players = JsonParser.parseString(body).asJsonObject.getAsJsonObject("players")
            if (players == null) {
                errors += "§cERROR: Custom name colors file is missing \"players\"."
                return
            }

            val remote = HashMap<String, Gradient>()
            for ((rawName, value) in players.entrySet()) {
                val name = rawName.trim()
                if (!VALID_NAME.matches(name)) {
                    errors += "§cERROR: Name colors: skipped '$rawName', not a valid username."
                    continue
                }
                val gradient = if (value.isJsonObject) parseGradient(value.asJsonObject) else null
                if (gradient == null) {
                    errors += "§cERROR: Name colors: skipped $name, no valid colors."
                    continue
                }
                remote[name] = gradient
            }

            gradients = BUILT_IN + remote
            remoteCount = remote.size
        } catch (e: Exception) {
            // Keep whatever we had last; a bad edit or outage shouldn't wipe colors.
            errors += "§cERROR: Couldn't load custom name colors: ${e.message ?: e.javaClass.simpleName}"
        } finally {
            report(errors)
        }
    }

    private fun report(errors: List<String>) {
        if (errors == lastErrors) return
        lastErrors = errors
        for (error in errors) {
            if (Meridian.mc.player == null) pendingErrors.add(error) else modMessage(error)
        }
    }

    private fun parseGradient(entry: JsonObject): Gradient? {
        val colors = entry.getAsJsonArray("colors") ?: return null
        val stops = colors.mapNotNull { el ->
            el.asString.trim().removePrefix("#").toIntOrNull(16)?.takeIf { it in 0..0xFFFFFF }
        }
        if (stops.isEmpty()) return null
        fun flag(key: String) = entry.get(key)?.asBoolean ?: false
        return Gradient(stops, flag("bold"), flag("italic"), flag("underlined"))
    }

    private fun get(url: String): String? {
        val request = HttpRequest.newBuilder(URI.create(url))
            .header("User-Agent", "Meridian")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        return if (response.statusCode() == 200) response.body() else null
    }

    @JvmStatic
    fun applyToNameTag(name: Component): Component = applyGradients(name)

    // Only call from the vanilla tab-list draw — see MixinPlayerTabOverlay for why.
    @JvmStatic
    fun applyToTabName(name: Component): Component = applyGradients(name)

    fun applyGradients(message: Component): Component {
        val table = gradients
        val full = message.string
        if (table.keys.none { full.contains(it) }) return message

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
                val match = table.entries.firstOrNull { (ign, _) ->
                    text.regionMatches(i, ign, 0, ign.length) && isWholeWord(text, i, ign.length)
                }
                if (match != null) {
                    flush()
                    out.append(gradient(match.key, match.value, style))
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
        // A legacy code right before the name ("§bChillyPine", as SkyHanni's compact
        // tab list builds it) is a boundary, not part of the word.
        val prevOk = start == 0 || !text[start - 1].isNameChar() ||
                (start >= 2 && text[start - 2] == '§')
        val nextOk = end >= text.length || !text[end].isNameChar()
        return prevOk && nextOk
    }

    private fun Char.isNameChar(): Boolean = isLetterOrDigit() || this == '_'

    private fun gradient(name: String, g: Gradient, sourceStyle: Style): Component {
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
            out.append(Component.literal(name[i].toString()).setStyle(base.withColor(sample(g.stops, t))))
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
