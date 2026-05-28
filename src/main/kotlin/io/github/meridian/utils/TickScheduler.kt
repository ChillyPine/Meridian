package io.github.meridian.utils

import io.github.meridian.Meridian.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import java.util.concurrent.CopyOnWriteArrayList

class TickTask internal constructor(
    private val delayTicks: Int,
    private val repeating: Boolean,
    private val task: () -> Unit,
) {
    private var ticks = 0
    @Volatile var cancelled: Boolean = false
        private set

    fun cancel() {
        cancelled = true
    }

    internal fun tick(): Boolean {
        if (cancelled) return true
        if (++ticks < delayTicks) return false
        mc.execute {
            if (!cancelled) task()
        }
        if (!repeating) return true
        ticks = 0
        return false
    }
}

// Schedules tasks on a tick clock. By default uses the server tick clock
// (driven by Hypixel's per-tick ClientboundPingPacket via ConnectionMixin),
// so countdowns stay aligned with the server even when it lags. Pass
// serverTick = false to fall back to the client tick clock for non-Hypixel
// or pre-login scheduling.
object TickScheduler {
    private val clientTasks = CopyOnWriteArrayList<TickTask>()
    private val serverTasks = CopyOnWriteArrayList<TickTask>()

    // Tracks how recently we've seen Hypixel's per-tick ping. When the server
    // tick clock is quiet (singleplayer, dev sim, non-Hypixel server), the
    // client tick clock pumps the server queue instead so scheduled tasks
    // still run. ~1s without a ping is well past normal jitter.
    @Volatile private var lastServerTickMs: Long = 0
    private const val SERVER_TICK_STALE_MS = 1000L

    fun schedule(ticks: Int, serverTick: Boolean = true, task: () -> Unit): TickTask {
        val t = TickTask(ticks, repeating = false, task = task)
        (if (serverTick) serverTasks else clientTasks).add(t)
        return t
    }

    fun repeat(ticks: Int, serverTick: Boolean = true, task: () -> Unit): TickTask {
        val t = TickTask(ticks, repeating = true, task = task)
        (if (serverTick) serverTasks else clientTasks).add(t)
        return t
    }

    fun cancelAll() {
        clientTasks.forEach { it.cancel() }
        serverTasks.forEach { it.cancel() }
        clientTasks.clear()
        serverTasks.clear()
    }

    @JvmStatic
    fun onServerTick() {
        lastServerTickMs = System.currentTimeMillis()
        pumpServerTasks()
    }

    private fun pumpServerTasks() {
        if (serverTasks.isEmpty()) return
        val done = serverTasks.filter { it.tick() }
        if (done.isNotEmpty()) serverTasks.removeAll(done)
    }

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            if (clientTasks.isNotEmpty()) {
                val done = clientTasks.filter { it.tick() }
                if (done.isNotEmpty()) clientTasks.removeAll(done)
            }
            if (System.currentTimeMillis() - lastServerTickMs > SERVER_TICK_STALE_MS) {
                pumpServerTasks()
            }
        })
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> cancelAll() }
    }
}