package io.github.meridian.commands
// §
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.meridian.Meridian
import io.github.meridian.features.impl.dungeons.CarryManager
import io.github.meridian.gui.HudEditScreen
import io.github.meridian.gui.MeridianScreen
import io.github.meridian.gui.ShitterListScreen
import io.github.meridian.features.impl.dungeons.ShitterList
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.simulateGameMessage
import io.github.meridian.gui.CalculatorScreen
import io.github.meridian.gui.CarryScreen
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component


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
                        literal("hud").executes { _ ->
                            // Defer: the chat screen is still closing as this lambda
                            // runs and would wipe a synchronously-set screen.
                            Meridian.mc.execute { Meridian.mc.setScreen(HudEditScreen()) }
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
                    .then(
                        literal("shitter")
                            .executes { ctx ->
                                sendShitterHelp(ctx.source)
                                1
                            }
                            .then(
                                literal("add").then(
                                    argument("players", StringArgumentType.greedyString())
                                        .executes { ctx ->
                                            ShitterList.addCommand(StringArgumentType.getString(ctx, "players"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("remove").then(
                                    argument("players", StringArgumentType.greedyString())
                                        .executes { ctx ->
                                            ShitterList.removeCommand(StringArgumentType.getString(ctx, "players"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("list")
                                    .executes { _ ->
                                        ShitterList.listCommand(1)
                                        1
                                    }
                                    .then(
                                        argument("page", IntegerArgumentType.integer(1))
                                            .executes { ctx ->
                                                ShitterList.listCommand(IntegerArgumentType.getInteger(ctx, "page"))
                                                1
                                            }
                                    )
                            )
                            .then(
                                literal("reset").executes { _ ->
                                    ShitterList.resetCommand()
                                    1
                                }
                            )
                            .then(
                                literal("gui").executes { _ ->
                                    Meridian.mc.execute { Meridian.mc.setScreen(ShitterListScreen()) }
                                    1
                                }
                            )
                            .then(
                                argument("argument", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        val arg = StringArgumentType.getString(ctx, "argument")
                                        modMessage("Unknown argument: '$arg'. Try /meridian help")
                                        0
                                    }
                            )

                    )
                    .then(
                        literal("carry")
                            .executes { ctx ->
                                sendCarryHelp(ctx.source)
                                1
                            }
                            .then(
                                literal("add_player").then(
                                    argument("players", StringArgumentType.greedyString())
                                        .executes { ctx ->
                                            CarryManager.addCommand(StringArgumentType.getString(ctx, "players"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("remove_player").then(
                                    argument("players", StringArgumentType.greedyString())
                                        .executes { ctx ->
                                            CarryManager.removeCommand(StringArgumentType.getString(ctx, "players"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("add_carry").then(
                                    argument("player", StringArgumentType.word())
                                        .executes { ctx ->
                                            CarryManager.addCarryCommand(StringArgumentType.getString(ctx, "player"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("remove_carry").then(
                                    argument("player", StringArgumentType.word())
                                        .executes { ctx ->
                                            CarryManager.removeCarryCommand(StringArgumentType.getString(ctx, "player"))
                                            1
                                        }
                                )
                            )
                            .then(
                                literal("reset").executes { _ ->
                                    CarryManager.resetCommand()
                                    1
                                }
                            )
                            .then(
                                literal("gui").executes { _ ->
                                    Meridian.mc.execute { Meridian.mc.setScreen(CarryScreen()) }
                                    1
                                }
                            )
                            .then(
                                argument("argument", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        val arg = StringArgumentType.getString(ctx, "argument")
                                        modMessage("Unknown argument: '$arg'. Try /meridian help")
                                        0
                                    }
                            )
                    )
                    .then(
                        literal("calc").executes { _ ->
                            Meridian.mc.execute { Meridian.mc.setScreen(CalculatorScreen()) }
                            1
                        }
                    )
                    .then(
                        argument("subcommand", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val sub = StringArgumentType.getString(ctx, "subcommand")
                                modMessage("Unknown subcommand: '$sub'. Try /meridian help")
                                0
                            }
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
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§r§6§lGeneral")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§6/meridian§f: Opens the GUI")
        sendClientMessage("§6/meridian help§f: Sends this dialogue")
        sendClientMessage("§6/meridian hud§f: Opens the HUD editor.")
        sendClientMessage("§6/meridian calc§f: Opens an in-game calculator.")
        sendClientMessage("§6/md§f: Alias")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§r§6§lShitter List Commands")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§6/md shitter add {IGN}§f: Adds player(s) to the shitter list. Separate multiple IGNs with spaces. Add a quoted reason: §7add Notch \"He cheated\"§f.")
        sendClientMessage("§6/md shitter remove {IGN}§f: Removes player(s) from the shitter list.")
        sendClientMessage("§6/md shitter list [#]§f: Displays the shitter list. Optionally jump to a page.")
        sendClientMessage("§6/md shitter reset§f: Clears the whole shitter list (asks for confirmation).")
        sendClientMessage("§6/md shitter gui§f: Opens the shitter list editor GUI.")
        sendClientMessage("§r§5§m                                                                              §r")
    }

    private fun sendShitterHelp(source: FabricClientCommandSource) {
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§r§6§lShitter List Commands")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§6/md shitter add {IGN}§f: Adds player(s) to the shitter list. Separate multiple IGNs with spaces. Add a quoted reason: §7add Notch \"He cheated\"§f.")
        sendClientMessage("§6/md shitter remove {IGN}§f: Removes player(s) from the shitter list.")
        sendClientMessage("§6/md shitter list [#]§f: Displays the shitter list. Optionally jump to a page.")
        sendClientMessage("§6/md shitter reset§f: Clears the whole shitter list (asks for confirmation).")
        sendClientMessage("§6/md shitter gui§f: Opens the shitter list editor GUI.")
        sendClientMessage("§r§5§m                                                                              §r")
    }

    private fun sendCarryHelp(source: FabricClientCommandSource) {
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§r§6§lCarry List Commands")
        sendClientMessage("§r§5§m                                                                              §r")
        sendClientMessage("§6/md Carry add {IGN}§f: Adds player(s) to the Carry list. Separate multiple IGNs with spaces.")
        sendClientMessage("§6/md Carry remove {IGN}§f: Removes player(s) from the Carry list.")
        sendClientMessage("§6/md Carry reset§f: Clears the whole Carry list (asks for confirmation).")
        sendClientMessage("§6/md Carry gui§f: Opens the Carry list editor GUI.")
        sendClientMessage("§r§5§m                                                                              §r")
    }
}