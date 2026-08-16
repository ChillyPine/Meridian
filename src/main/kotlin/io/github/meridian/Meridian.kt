package io.github.meridian

import io.github.meridian.commands.MeridianCommand
import io.github.meridian.events.MeridianEvents
import io.github.meridian.features.FeatureList
import io.github.meridian.features.FeatureManager
import io.github.meridian.hud.HudManager
import io.github.meridian.features.impl.general.ChatBlockerRegistry
import io.github.meridian.utils.ChatBlocker
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.NameGradients
import io.github.meridian.utils.F4State
import io.github.meridian.utils.F5State
import io.github.meridian.utils.F6State
import io.github.meridian.utils.P2State
import io.github.meridian.utils.P5State
import io.github.meridian.utils.SkyblockLocation
import io.github.meridian.utils.TickScheduler
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.minecraft.client.Minecraft

object Meridian : ClientModInitializer {
    @JvmStatic
    val mc: Minecraft = Minecraft.getInstance()
    const val MOD_ID = "meridian"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitializeClient() {
        MeridianCommand.register()
        ChatBlocker.init()
        NameGradients.init()
        TickScheduler.init()
        Keybinds.init()
        SkyblockLocation.init()
        F4State.init()
        F5State.init()
        F6State.init()
        P2State.init()
        P5State.init()
        MeridianEvents.init()
        FeatureList.registerAll()
        ChatBlockerRegistry.init()
        FeatureManager.load()
        HudManager.init()
        logger.info("Meridian loaded")
    }
}