package best.spaghetcodes.duckdueller.control

import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.Config
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import org.lwjgl.input.Keyboard

object Commands {

    /**
     * /duck command
     */
    class DuckCommand : CommandBase() {
        override fun getCommandName(): String {
            return "duck"
        }

        override fun getCommandUsage(sender: ICommandSender?): String {
            return "/duck help"
        }

        override fun getRequiredPermissionLevel(): Int {
            return 0
        }

        override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
            if (args != null && args.isNotEmpty()) {
                when (args[0]) {
                    "help" -> {
                        ChatUtils.info("${EnumChatFormatting.DARK_GRAY}--------------${EnumChatFormatting.WHITE} Duck Help ${EnumChatFormatting.DARK_GRAY}--------------")
                        ChatUtils.info("Toggle the bot using ${EnumChatFormatting.BOLD}${Keyboard.getKeyName(KeyBindings.toggleBotKeyBinding.keyCode)}")
                        ChatUtils.info("")
                        ChatUtils.info("/duck minCPS <cps> - ${EnumChatFormatting.ITALIC}Minimum CPS of the bot")
                        ChatUtils.info("/duck maxCPS <cps> - ${EnumChatFormatting.ITALIC}Maximum CPS of the bot")
                        ChatUtils.info("/duck webhook <url> - ${EnumChatFormatting.ITALIC}Discord WebHook URL to log games to")
                        ChatUtils.info("/duck ggMessage <message> - ${EnumChatFormatting.ITALIC}AutoGG Message")
                        ChatUtils.info("/duck ggDelay <ms> - ${EnumChatFormatting.ITALIC}Delay to wait before sending ${EnumChatFormatting.BOLD}ggMessage")
                        ChatUtils.info("/duck rqDelay <ms> - ${EnumChatFormatting.ITALIC}Delay to wait before re-queueing")
                        ChatUtils.info("/duck maxDistanceLook <blocks> - ${EnumChatFormatting.ITALIC}Bot view distance")
                        ChatUtils.info("/duck maxDistanceAttack <blocks> - ${EnumChatFormatting.ITALIC}Bot attack distance")
                        ChatUtils.info("${EnumChatFormatting.DARK_GRAY}--------------------------------------")
                    }
                    else -> {
                        // make sure the value is in the config
                        if (Config.get(args[0]) != null) {
                            // either print the current config value or set a new one
                            if (args.size >= 2) {
                                when(Config.get(args[0])) {
                                    is Int -> {
                                        try {
                                            Config.set(args[0], args[1].toInt())
                                            ChatUtils.info("${EnumChatFormatting.GREEN}Successfully set ${args[0]} to ${EnumChatFormatting.BOLD}${args[1]}")
                                        } catch (e: Exception) {
                                            ChatUtils.error("Invalid number: ${args[1]}")
                                        }
                                    }
                                    is String -> {
                                        // combine the rest of the args into a string
                                        var message = ""
                                        for (i in 1 until args.size) {
                                            message += args[i] + " "
                                        }
                                        Config.set(args[0], message)
                                        ChatUtils.info("${EnumChatFormatting.GREEN}Successfully set ${args[0]} to ${EnumChatFormatting.BOLD}$message")
                                    }
                                }
                                Config.save()
                            } else {
                                ChatUtils.info("${EnumChatFormatting.BOLD}${args[0]}${EnumChatFormatting.RESET} is currently set to: ${Config.get(args[0])}")
                            }
                        }
                    }
                }
            } else {
                ChatUtils.error("Invalid usage! Use ${EnumChatFormatting.WHITE}/duck help${EnumChatFormatting.RED} for help.")
            }
        }

    }

    fun registerCommands() {
        println("Registering commands")

        ClientCommandHandler.instance.registerCommand(DuckCommand())
    }

}