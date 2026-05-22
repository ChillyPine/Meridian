package io.github.meridian.commands
// §
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.meridian.Meridian
import io.github.meridian.gui.MeridianScreen
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.simulateGameMessage
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object MeridianCommand {
    fun register() {
        Meridian.logger.info("Registering /meridian command callback")
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            Meridian.logger.info("ClientCommandRegistrationCallback fired — registering /meridian and /md")
            val root = dispatcher.register(
                literal("meridian")
                    .executes { ctx ->
                        openGui(ctx.source)
                        1
                    }
                    .then(
                        literal("help").executes { ctx ->
                            sendHelp(ctx.source)
                            1
                        }
                    )
                    .then(
                        literal("simulate")
                            .then(
                                argument("text", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        val text = StringArgumentType.getString(ctx, "text")
                                        simulateGameMessage(text)
                                        1
                                    }
                            )
                    )
            )

            dispatcher.register(
                literal("md")
                    .executes(root.command)
                    .redirect(root)
            )
        }
    }

    private fun openGui(source: FabricClientCommandSource) {
        Meridian.logger.info("openGui called — deferring setScreen to next tick")
        Meridian.mc.execute {
            Meridian.mc.setScreen(MeridianScreen())
        }
    }

    private fun sendHelp(source: FabricClientCommandSource) {
        sendClientMessage("§r§7§m                                                                              §r")
        sendClientMessage("§6/meridian§f: Opens the GUI")
        sendClientMessage("§6/md§f: Alias")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§r§6Shitter List Commands")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§6/shitter add {IGN}§f: Adds a player to the shitter list. You can add multiple at once by separating the IGNs with spaces.")
        sendClientMessage("§6/shitter remove {IGN}§f: Removes a player from the shitter list. You can remove multiple at once by separating the IGNs with spaces.")
        sendClientMessage("§6/shitter list§f: Displays the shitter list. Use /shitter list [#] to display a certain page.")
        sendClientMessage("§6/shitter reset§f: Removes every player on the shitter list. Requires confirmation before resetting.")
        sendClientMessage("§r§5§m                                                                              §r")
    }
}