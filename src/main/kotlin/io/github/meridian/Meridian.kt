package io.github.meridian

import io.github.meridian.commands.MeridianCommand
import io.github.meridian.features.FeatureList
import io.github.meridian.features.FeatureManager
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.ChatBlocker
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.F4State
import io.github.meridian.utils.F5State
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
        TickScheduler.init()
        DungeonState.init()
        F4State.init()
        F5State.init()
        FeatureList.registerAll()
        FeatureManager.load()
        HudManager.init()
        logger.info("Meridian loaded")
    }
}