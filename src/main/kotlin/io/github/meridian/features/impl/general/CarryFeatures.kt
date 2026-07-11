package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.impl.dungeons.CarryManager
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.ESP
import net.minecraft.world.entity.decoration.ArmorStand

object AutoTrackClientProgress : SwitchFeature(
    name = "Track Client Progress",
    description = "Automatically tracks clients bosses. Completed and Total",
    category = "General",
    configKey = "auto_track_client_progress",
    subcategory = "Carry",
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

                val client = clients.firstOrNull {
                    name.contains(it, ignoreCase = true)
                } ?: continue

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
                    CarryManager.addCarry(client)
                    state.completed = true
                    iterator.remove()
                }
            }
        }
    }
}


object BoxClientsBosses : SwitchFeature(
    name = "Box Clients Boss",
    description = "Gives each unique client a different color box for their boss. ",
    category = "General",
    configKey = "box_clients_bosses",
    subcategory = "Carry",
) {
    // cycles different colors for each unique player being carried. for the love of god if you have more people than this.
    private val palette = intArrayOf(
        0x8000A2FF.toInt(), // blue
        0x80FF3333.toInt(), // red
        0x8033FF57.toInt(), // green
        0x80FFC300.toInt(), // yellow
        0x80C300FF.toInt(), // purple
        0x8000FFF7.toInt(), // cyan
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
                val index = list.indexOfFirst { name.contains(it, ignoreCase = true) }
                if (index == -1) continue
                ESP.drawBox(ctx, ent, w = 0.75, h = 2.0, wz = 0.75, yOffset = -2.2, argb = colorFor(index))
            }
        }
    }
}