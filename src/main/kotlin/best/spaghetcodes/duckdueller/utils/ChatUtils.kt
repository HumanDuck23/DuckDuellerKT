package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

object ChatUtils {

    fun info(message: String) {
        sendChatMessage("${EnumChatFormatting.GOLD}[${EnumChatFormatting.LIGHT_PURPLE}Duck ${EnumChatFormatting.DARK_PURPLE}Dueller${EnumChatFormatting.GOLD}] ${EnumChatFormatting.WHITE}$message")
    }

    fun error(message: String) {
        sendChatMessage("${EnumChatFormatting.GOLD}[${EnumChatFormatting.LIGHT_PURPLE}Duck ${EnumChatFormatting.DARK_PURPLE}Dueller${EnumChatFormatting.GOLD}] ${EnumChatFormatting.RED}$message")
    }

    fun sendChatMessage(message: String) {
        DuckDueller.mc.thePlayer.addChatMessage(ChatComponentText(message))
    }

}