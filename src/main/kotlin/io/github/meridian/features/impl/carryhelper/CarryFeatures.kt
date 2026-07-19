package io.github.meridian.features.impl.carryhelper

import io.github.meridian.Meridian
import io.github.meridian.features.impl.dungeons.CarryManager
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.sendCommand
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// TODO: Change behavior after trade detection
// TODO: Add Count Client Deaths as Kill
// TODO: Add Warn if Healer (after we add class detection)
// TODO: Add Price Checker


internal object ClientTimingData {

    // spawn timestamp of a client's *current* boss cycle. Set when the boss is first
    // detected, consumed (removed) when it dies, to compute that cycle's kill time.
    val spawnTimes = mutableMapOf<String, Long>()

    // timestamp of a client's last death, used to measure the gap until their next spawn.
    val lastDeathTimes = mutableMapOf<String, Long>()

    // up to the last 3 valid "gap between death and next spawn" durations (ms), oldest first.
    private val spawnDurations = mutableMapOf<String, MutableList<Long>>()

    // up to the last 3 valid "spawn to death" kill durations (ms), oldest first.
    private val killDurations = mutableMapOf<String, MutableList<Long>>()

    private const val HISTORY_SIZE = 3

    fun recordSpawn(client: String, durationMs: Long) = record(spawnDurations, client, durationMs)
    fun recordKill(client: String, durationMs: Long) = record(killDurations, client, durationMs)

    private fun record(map: MutableMap<String, MutableList<Long>>, client: String, durationMs: Long) {
        val list = map.getOrPut(client) { mutableListOf() }
        list.add(durationMs)
        if (list.size > HISTORY_SIZE) list.removeAt(0)
    }

    // last 3 vaild spawn and kill times for session timer
    fun averageCycleDuration(client: String): Long? {
        val spawns = spawnDurations[client] ?: return null
        val kills = killDurations[client] ?: return null
        if (spawns.size < HISTORY_SIZE || kills.size < HISTORY_SIZE) return null
        return (spawns.sum() + kills.sum()) / HISTORY_SIZE
    }

    fun clear(client: String) {
        spawnTimes.remove(client)
        lastDeathTimes.remove(client)
        spawnDurations.remove(client)
        killDurations.remove(client)
    }
}

// formats timing messages
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${s}s"
        else -> "${s}s"
    }
}

object AutoTrackClientProgress : SwitchFeature(
    name = "Track Client Boss Progress",
    description = "Automatically tracks clients bosses (completed and total).",
    category = "Carry Helper",
    configKey = "auto_track_client_progress",
    subcategory = "General",
) {
    private data class BossState(
        var lastSeen: Long,
        var completed: Boolean = false
    )

    private val trackedBosses = mutableMapOf<String, BossState>()

    // boss must be gone this long before assuming it died
    private const val DESPAWN_DELAY = 1000L

    init {
        onTick {

            val level = Meridian.mc.level ?: return@onTick
            val player = Meridian.mc.player ?: return@onTick
            val clients = CarryManager.all()

            val now = System.currentTimeMillis()

            // Update every boss currently visible
            for (entity in level.entitiesForRendering()) {

                if (entity !is ArmorStand) continue
                if (entity.distanceToSqr(player.x, player.y, player.z) > 100.0 * 100.0) continue

                val name = entity.customName?.string ?: continue

                val client = clients
                    .sortedByDescending { it.length }
                    .firstOrNull { name.contains(it, ignoreCase = true) }
                    ?: continue

                val key = client.lowercase()

                trackedBosses.getOrPut(key) {
                    announceSpawnTime(key, client, now)
                    BossState(now)
                }.apply {
                    lastSeen = now
                }
            }

            // anything unseen long enough is assumed dead
            val iterator = trackedBosses.iterator()
            while (iterator.hasNext()) {
                val (client, state) = iterator.next()
                if (!state.completed && now - state.lastSeen > DESPAWN_DELAY) {
                    val completed = CarryManager.addCarry(client)
                    if (completed != null) {
                        val ordered = CarryManager.orderedFor(client)
                        val clientIGN = CarryManager.all().firstOrNull { it.equals(client, ignoreCase = true) } ?: client

                        if (TrackClientKillTime.enabled) {
                            val spawnTime = ClientTimingData.spawnTimes.remove(client)
                            if (spawnTime != null) {
                                val killDuration = now - spawnTime
                                ClientTimingData.recordKill(client, killDuration)
                                modMessage("§b$clientIGN §btook §6${formatDuration(killDuration)} §bto kill")
                            }
                        }

                        // recorded regardless of kill-time success, so the *next* spawn
                        // always has a death to measure its gap from
                        if (TrackClientSpawnTime.enabled) {
                            ClientTimingData.lastDeathTimes[client] = now
                        }

                        modMessage("§b$clientIGN §f(§6$completed§f/§e$ordered§f)")

                        if (AnnounceProgressClient.enabled) {
                            CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS)
                                .execute { sendCommand("pc [Meridian] $clientIGN ($completed/$ordered)") }
                        }

                        if (TrackSessionTime.enabled) {
                            announceSessionTime(client, clientIGN, ordered, completed)
                        }

                        if (ordered > 0 && completed >= ordered) {
                            CarryManager.completeCarry(client)
                            modMessage("§b$clientIGN §ahas completed their order and was removed from the list.")
                            ClientTimingData.clear(client)
                        }
                    }
                    state.completed = true
                    iterator.remove()
                }
            }
        }
    }

    // clients spawn time
    private fun announceSpawnTime(key: String, clientIGN: String, now: Long) {
        if (!TrackClientSpawnTime.enabled) return

        val lastDeath = ClientTimingData.lastDeathTimes[key]
        ClientTimingData.spawnTimes[key] = now

        if (lastDeath == null) return

        val spawnDuration = now - lastDeath
        ClientTimingData.recordSpawn(key, spawnDuration)
        modMessage("§b$clientIGN §btook §6${formatDuration(spawnDuration)} §bto spawn a boss")
    }
    // session time dogshit
    private fun announceSessionTime(client: String, clientIGN: String, ordered: Int, completed: Int) {
        if (ordered <= 0) return
        val remaining = ordered - completed
        if (remaining <= 0) return

        val avgCycleMs = ClientTimingData.averageCycleDuration(client) ?: return
        val remainingMs = avgCycleMs * remaining

        val hours = remainingMs / 3_600_000.0
        val eta = LocalDateTime.now().plus(Duration.ofMillis(remainingMs))
        val hour12 = if (eta.hour % 12 == 0) 12 else eta.hour % 12
        val ampm = if (eta.hour < 12) "am" else "pm"
        val timeStr = String.format("%d:%02d%s", hour12, eta.minute, ampm)

        modMessage("§bSession for §f$clientIGN §bis estimated to take §6${String.format("%.2f", hours)} §bhours §f(§e$timeStr§f)")
    }
}

object AutoTrackDungeonProgress : SwitchFeature(
    name = "Track Client Dungeon Progress",
    description = "Automatically tracks clients dungeons carries (completed and total).",
    category = "Carry Helper",
    configKey = "auto_track_client_progress_dungeons",
    subcategory = "General",
) {

    init {

        val TEAM_SCORE = Regex("^ *Team Score: (\\d+) \\(([\\w+]{1,2})\\)$")

        onChat { text, _, _ ->
            if (!text.matches(TEAM_SCORE)) return@onChat

            val clients = CarryManager.all()
            if (clients.isEmpty()) return@onChat

            for (client in clients) {
                val completed = CarryManager.addCarry(client) ?: continue
                val ordered = CarryManager.orderedFor(client)

                modMessage("§b$client §f(§6$completed§f/§e${if (ordered > 0) "$ordered" else "-"}§f)")

                if (ordered > 0 && completed >= ordered) {
                    CarryManager.completeCarry(client)
                    modMessage("§b$client §ahas completed their order and was removed from the list.")
                }
            }

        }
    }
}

object HighlightClients : SwitchFeature(
    name = "Highlight Client(s)",
    description = "Highlights the client(s) using the default vanilla glow.\nDistinct from other box features.",
    category = "Carry Helper",
    configKey = "highlight_clients",
    subcategory = "General",
) {
    /**
     * Whether [entity] should glow: the feature is on and it's a player on the
     * carry list. Client-side `setGlowingTag` is a no-op (it sets the synced flag
     * to the current — false — value), so glow is driven by MixinEntity forcing
     * `isCurrentlyGlowing` to return this instead.
     */
    fun shouldGlow(entity: Entity): Boolean {
        if (!enabled) return false
        if (entity !is Player) return false
        val ign = entity.gameProfile.name ?: return false
        return CarryManager.all().any { it.equals(ign, ignoreCase = true) }
    }

    /**
     * RGB glow color for [entity] (matching its boss box color from
     * [BoxClientsBosses]), or null if it's not a tracked client / the feature is
     * off. Called from MixinEntityRenderer's outline-color redirect; masked to
     * RGB since team color carries no alpha.
     */
    fun glowColorFor(entity: Entity): Int? {
        if (entity !is Player) return null
        if (!shouldGlow(entity)) return null
        val ign = entity.gameProfile.name ?: return null
        val idx = CarryManager.all().indexOfFirst { it.equals(ign, ignoreCase = true) }
        if (idx < 0) return null
        return BoxClientsBosses.colorFor(idx) and 0xFFFFFF
    }
}

object BoxClientsBosses : SwitchFeature(
    name = "Box Clients Boss",
    description = "Boxes the slayer boss of the client.\nEach unique client has a different color for their boss.",
    category = "Carry Helper",
    configKey = "box_clients_bosses",
    subcategory = "General",
) {
    // cycles different colors for each unique player being carried. for the love of god if you have more people than this.
    private val palette = intArrayOf(
        0x8000A2FF.toInt(), 0x80FF3333.toInt(), 0x8033FF57.toInt(), 0x80C300FF.toInt(), 0x80FFC300.toInt(), 0x8000FFF7.toInt(),
    )

    internal fun colorFor(index: Int): Int = palette[index % palette.size]

    init {
        onRender { ctx ->
            val level = Meridian.mc.level ?: return@onRender
            val player = Meridian.mc.player ?: return@onRender
            val list = CarryManager.all()
            if (list.isEmpty()) return@onRender

            for (ent in level.entitiesForRendering()) {
                if (ent !is ArmorStand) continue
                if (ent.distanceToSqr(player.x, player.y, player.z) > 100 * 100) continue
                val name = ent.customName?.string ?: continue

                //if names are similar just like dont cook them
                val match = list.firstOrNull { c ->
                    Regex("\\b${Regex.escape(c)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(name)
                } ?: continue
                val index = list.indexOf(match)

                ESP.drawBox(ctx, ent, w = 0.75, h = 2.0, wz = 0.75, yOffset = -2.2, argb = colorFor(index))
                if (DrawLineToClientBoss.enabled) {
                    val p = ent.getPosition(Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true))
                    ESP.drawTracer(ctx, p.x, p.y, p.z, colorFor(index))
                }

            }
        }
    }
}

object AnnounceProgressClient : SwitchFeature(
    name = "Announce Clients Progress",
    description = "In party chat -> IGN ( completed / total )",
    category = "Carry Helper",
    configKey = "announce_client_progress",
    subcategory = "General",
    dependsOn = AutoTrackClientProgress
)

object DrawLineToClientBoss : SwitchFeature(
    name = "Boss Tracer",
    description = "Draws tracer to clients bosses",
    category = "Carry Helper",
    configKey = "draw_line_to_client_boss",
    subcategory = "General",
    dependsOn = BoxClientsBosses
)

object TrackClientSpawnTime : SwitchFeature(
    name = "Track Client Spawn Time",
    description = "Announces how long it took a client's next boss to spawn after their last kill. Starts timing once the first boss has spawned and died.",
    category = "Carry Helper",
    configKey = "track_client_spawn_time",
    subcategory = "General",
    dependsOn = AutoTrackClientProgress
)

object TrackClientKillTime : SwitchFeature(
    name = "Track Client Kill Time",
    description = "Announces how long a client's boss took to die once it spawned, keeping the last 3 per client.",
    category = "Carry Helper",
    configKey = "track_client_kill_time",
    subcategory = "General",
    dependsOn = AutoTrackClientProgress
)

object TrackSessionTime : SwitchFeature(
    name = "Track Session Time",
    description = "Once a client has 3 valid spawn times and 3 valid kill times, averages them against their remaining carries and announces an ETA after every kill.",
    category = "Carry Helper",
    configKey = "track_session_time",
    subcategory = "General",
    dependsOn = AutoTrackClientProgress
)

object AutoDetectTrade : SwitchFeature(
    name = "Detect Trade",
    description = "Detects trades and prompts you to open the carry manager GUI.",
    category = "Carry Helper",
    configKey = "detect_trade_carry",
    subcategory = "General",
) {
    private val traderegex =
        Regex("^Trade completed with (?:(.+) )?(\\w+)!\$")

    init {

        onChat { text, _, _ ->
            if (!text.matches(traderegex)) return@onChat
            val line = buildButtons()
            CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
                .execute { sendClientMessage(line) }
        }
    }
    private fun buildButtons(): MutableComponent {
        val line = Component.literal("§6[MD] §f» ")
        line.append(button("§a[Open Carry Manager] ", "/md carry gui", "§aOpens the Carry Manager"))
        return line
    }

    private fun button(label: String, command: String, hover: String): MutableComponent =
        Component.literal(label).withStyle { style ->
            style.withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
                .withClickEvent(ClickEvent.RunCommand(command))
        }
}