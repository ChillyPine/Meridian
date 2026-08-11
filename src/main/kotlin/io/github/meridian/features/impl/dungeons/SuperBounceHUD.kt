package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.BasicState
import io.github.meridian.utils.P2State
import io.github.meridian.utils.P5State
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel

object SuperBounceHUD : SwitchFeature(
    name = "Super Bounce Display",
    description = "Shows an on-screen HUD element with the current status of whether you will or won't superbounce based on your current pitch.\nEdit the element position with /md hud.",
    category = "Dungeons",
    configKey = "super_bounce_hud",
    subcategory = "P3",
) {
    private const val SUPER_PITCH = -40f

    private val inP3 = BasicState(false)
    private var lastLevel: ClientLevel? = null

    @Volatile private var line: String? = null

    private val element = object : HudElement(
        id = "super_bounce_hud",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.6f,
    ) {
        override fun content(): List<String> = line?.let { listOf(it) } ?: emptyList()
        override fun preview(): List<String> = listOf("§a§lNORMAL")
    }

    init {
        HudManager.register(element)

        P2State.state.listen { inP2 -> if (!inP2) inP3.value = true }
        P5State.state.listen { inP5 -> if (inP5) inP3.value = false }
        inP3.listen { if (!it) line = null }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                inP3.value = false
            }
        })

        onTick(inP3) {
            val pitch = mc.player?.xRot ?: run { line = null; return@onTick }
            line = if (pitch <= SUPER_PITCH) "§c§lSUPER" else "§a§lNORMAL"
        }
    }

    override fun onDeactivate() {
        line = null
    }
}
