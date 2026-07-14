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
import net.minecraft.world.entity.decoration.ArmorStand
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// TODO: Change behavior after trade detection
// TODO: Add Count Client Deaths as Kill
// TODO: Add Draw Line to Clients Boss
// TODO: Add Track Client Spawn Time
// TODO: Add Track Client Kill Time
// TODO: Add Get Session Time (maybe)
// TODO: Add Warn if Healer (after we add class detection)
// TODO: Add Price Checker

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

                trackedBosses.getOrPut(client.lowercase()) {
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

                        modMessage("§b$clientIGN §f(§6$completed§f/§e$ordered§f)")

                        if (AnnounceProgressClient.enabled) {
                            CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS)
                                .execute { sendCommand("pc [Meridian] $clientIGN ($completed/$ordered)") }
                        }

                        if (ordered > 0 && completed >= ordered) {
                            CarryManager.completeCarry(client)
                            modMessage("§b$clientIGN §ahas completed their order and was removed from the list.")
                        }
                    }
                    state.completed = true
                    iterator.remove()
                }
            }
        }
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

    private fun colorFor(index: Int): Int = palette[index % palette.size]

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