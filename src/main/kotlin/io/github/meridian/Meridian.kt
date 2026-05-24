package io.github.meridian

import io.github.meridian.commands.MeridianCommand
import io.github.meridian.features.FeatureList
import io.github.meridian.features.FeatureManager
import io.github.meridian.utils.ChatBlocker
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
        FeatureList.registerAll()
        FeatureManager.load()
        logger.info("Meridian loaded")
    }
}