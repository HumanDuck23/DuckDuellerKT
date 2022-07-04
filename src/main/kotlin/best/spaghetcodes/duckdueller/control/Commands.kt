package best.spaghetcodes.duckdueller.control

import best.spaghetcodes.duckdueller.utils.ChatUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler

object Commands {

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
                ChatUtils.info("${EnumChatFormatting.DARK_GRAY}--------------${EnumChatFormatting.WHITE} Duck Help ${EnumChatFormatting.DARK_GRAY}--------------")
                ChatUtils.info("Nothing here yet, have some bread.")
                ChatUtils.info("${EnumChatFormatting.DARK_GRAY}--------------------------------------")
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