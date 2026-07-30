package io.github.meridian.features.impl.general

import io.github.meridian.Meridian
import io.github.meridian.features.types.ButtonFeature
import io.github.meridian.utils.sendCommand
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// § codes render in the disconnect screen the same way they do in chat
// (StringDecomposer processes legacy codes), so a plain literal is enough.
private val FAKE_BAN_REASON: Component = Component.literal(
    "§cYou are temporarily banned for §f29d 23h 59m 59s§c from this server!\n\n" +
        "§7Reason: §rCheating through the use of unfair game advantages.\n" +
        "§7Find out more: §b§nhttps://www.hypixel.net/appeal\n\n" +
        "§7Ban ID: §r#8CAC276C\n" +
        "§7Sharing your Ban ID may affect the processing of your appeal!"
)

object NonRemover : ButtonFeature(
    name = "Non Remover",
    description = "Removes Nons from Skyblock.",
    category = "General",
    configKey = "non_remover",
    subcategory = "Miscellaneous",
    buttonLabel = "Click",
    onClick = {
        Meridian.mc.execute { Meridian.mc.setScreen(null) }
        CompletableFuture.delayedExecutor(2500, TimeUnit.MILLISECONDS).execute {
            sendCommand("limbo", delayMs = 0)
        }
        CompletableFuture.delayedExecutor(4500, TimeUnit.MILLISECONDS).execute {
            Meridian.mc.execute {
                Meridian.mc.connection?.connection?.disconnect(FAKE_BAN_REASON)
            }
        }
    }
)
