package io.github.meridian.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.meridian.Meridian.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.multiplayer.ClientLevel

/**
 * Islands as reported by the `mode` field of `/locraw`. The ids are Hypixel's and are stable —
 * never change one. Display names are ours and are only ever shown to the user.
 */
enum class Island(val id: String, val displayName: String) {
    PRIVATE_ISLAND("dynamic", "Private Island"),
    GARDEN("garden", "Garden"),
    HUB("hub", "Hub"),
    THE_FARMING_ISLANDS("farming_1", "The Farming Islands"),
    THE_PARK("foraging_1", "The Park"),
    GALATEA("foraging_2", "Galatea"),
    TORRHUS_CANYON("foraging_3", "Torrhus Canyon"),
    SPIDERS_DEN("combat_1", "Spider's Den"),
    THE_END("combat_3", "The End"),
    CRIMSON_ISLE("crimson_isle", "Crimson Isle"),
    KUUDRAS_HOLLOW("kuudra", "Kuudra's Hollow"),
    GOLD_MINE("mining_1", "Gold Mine"),
    DEEP_CAVERNS("mining_2", "Deep Caverns"),
    DWARVEN_MINES("mining_3", "Dwarven Mines"),
    GLACITE_MINESHAFTS("mineshaft", "Glacite Mineshafts"),
    CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows"),
    BACKWATER_BAYOU("fishing_1", "Backwater Bayou"),
    LOTUS_ATOLL("lotus_atoll", "Lotus Atoll"),
    SAFARI("safari", "Safari"),
    DUNGEON_HUB("dungeon_hub", "Dungeon Hub"),
    DUNGEON("dungeon", "Dungeons"),
    DARK_AUCTION("dark_auction", "Dark Auction"),
    JERRYS_WORKSHOP("winter", "Jerry's Workshop"),
    THE_RIFT("rift", "The Rift"),

    /** Not on Skyblock, in a lobby, or `/locraw` hasn't answered yet. */
    UNKNOWN("unknown", "Unknown");

    companion object {
        private val byId = entries.associateBy { it.id }
        fun from(id: String?): Island = byId[id] ?: UNKNOWN
    }
}

val FORAGING_ISLANDS = setOf(Island.THE_PARK, Island.GALATEA, Island.TORRHUS_CANYON)
val MINING_ISLANDS = setOf(
    Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES,
    Island.GLACITE_MINESHAFTS, Island.CRYSTAL_HOLLOWS
)

/**
 * Where the player is, on two levels:
 *
 * - **[island]** — which Skyblock island, from `/locraw`. Authoritative but asynchronous: we send
 *   the command on every world change and the answer lands a moment later.
 * - **[area]** — the sub-area within the island (`Howling Cave`, `Wizard Tower`, …), read off the
 *   `⏣` line of the sidebar. Instant and needs no command, but area names repeat across islands,
 *   so **always pair an area check with an island check.**
 *
 * Both are reactive: pass [on] / [inArea] as the `gate` argument to `SwitchFeature.onRender` etc.
 * and the listener physically detaches when you leave.
 */
object SkyblockLocation {
    private const val AREA_ICON = '⏣'
    private const val RIFT_AREA_ICON = 'ф'
    private const val AREA_SCAN_INTERVAL = 4
    private const val REQUEST_DELAY_TICKS = 20
    private const val RETRY_DELAY_TICKS = 40
    private const val MAX_RETRIES = 3

    private val _island = BasicState(Island.UNKNOWN)
    val islandState: State<Island> = _island
    val island: Island get() = _island.value

    private val _area = BasicState("")
    val areaState: State<String> = _area
    val area: String get() = _area.value

    private val _onSkyblock = BasicState(false)
    val onSkyblockState: State<Boolean> = _onSkyblock
    val onSkyblock: Boolean get() = _onSkyblock.value

    /** Raw `/locraw` extras, kept for debugging and for features that care about lobby vs game. */
    var server: String = ""
        private set
    var gameType: String = ""
        private set
    var map: String = ""
        private set

    val onHypixel: Boolean
        get() {
            val ip = mc.currentServer?.ip?.lowercase() ?: ""
            if (ip.contains("hypixel.net") || ip.contains("hypixel.io")) return true
            return mc.player?.connection?.serverBrand()?.contains("Hypixel") == true
        }

    /** A gate that is true only on [islands]. */
    fun on(vararg islands: Island): State<Boolean> {
        val set = islands.toSet()
        return islandState.map { it in set }
    }

    fun on(islands: Set<Island>): State<Boolean> = islandState.map { it in islands }

    /** A gate that is true only in the named sub-[areas]. Combine with [on] — area names collide. */
    fun inArea(vararg areas: String): State<Boolean> {
        val set = areas.toSet()
        return areaState.map { it in set }
    }

    private var lastLevel: ClientLevel? = null
    private var awaitingLocraw = false
    private var retries = 0
    private var pendingRequest: TickTask? = null
    private var scanCounter = 0

    fun init() {
        // Parsed inside the block rule rather than a GAME listener: vetoing the message stops GAME
        // from ever firing, so the parse has to happen on the veto path. Only the reply to a
        // request we made is hidden — a manually typed /locraw still prints.
        ChatBlocker.add { component ->
            val raw = component.string
            if (!raw.startsWith("{\"server\":")) return@add false
            parseLocraw(raw)
            val requested = awaitingLocraw
            awaitingLocraw = false
            pendingRequest?.cancel()
            requested
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                onLevelChange()
            }
            if (++scanCounter >= AREA_SCAN_INTERVAL) {
                scanCounter = 0
                updateArea()
            }
        })

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> reset() }
    }

    /**
     * Hypixel warps you to a new backend server without dropping the connection, so a world swap is
     * the only signal that the island may have changed. Island is cleared immediately — better for a
     * gate to read UNKNOWN for a second than to claim the island we just left. Area is left alone;
     * the sidebar repopulates it within a few ticks, well before /locraw answers.
     */
    private fun onLevelChange() {
        _island.value = Island.UNKNOWN
        retries = 0
        pendingRequest?.cancel()
        if (mc.level == null || !onHypixel) return
        pendingRequest = TickScheduler.schedule(REQUEST_DELAY_TICKS, serverTick = false) { request() }
    }

    /** Ask the server where we are. Retries a few times in case the world loaded before chat was ready. */
    private fun request() {
        if (!onHypixel) return
        awaitingLocraw = true
        sendCommand("locraw", delayMs = 0)
        pendingRequest = TickScheduler.schedule(RETRY_DELAY_TICKS, serverTick = false) {
            if (!awaitingLocraw) return@schedule
            if (retries++ < MAX_RETRIES) request() else awaitingLocraw = false
        }
    }

    private fun parseLocraw(json: String) {
        val obj = runCatching { JsonParser.parseString(json).asJsonObject }.getOrNull() ?: return
        server = obj.str("server")
        gameType = obj.str("gametype")
        map = obj.str("map")
        _onSkyblock.value = gameType == "SKYBLOCK"
        // A lobby reply has no "mode" at all, which is exactly UNKNOWN.
        _island.value = if (onSkyblock) Island.from(obj.str("mode").ifEmpty { null }) else Island.UNKNOWN
    }

    private fun JsonObject.str(key: String): String =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString ?: ""

    private fun updateArea() {
        val line = sidebarLines().firstOrNull { it.indexOf(AREA_ICON) >= 0 || it.indexOf(RIFT_AREA_ICON) >= 0 }
        _area.value = line?.replace(AREA_ICON, ' ')?.replace(RIFT_AREA_ICON, ' ')?.trim() ?: ""
    }

    private fun reset() {
        pendingRequest?.cancel()
        pendingRequest = null
        awaitingLocraw = false
        retries = 0
        lastLevel = null
        server = ""
        gameType = ""
        map = ""
        _island.value = Island.UNKNOWN
        _area.value = ""
        _onSkyblock.value = false
    }
}