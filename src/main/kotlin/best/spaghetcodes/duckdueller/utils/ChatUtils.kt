package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

object ChatUtils {

    fun removeFormatting(text: String): String{
        var t = ""
        var skip = false
        for (i in text.indices) {
            if (!skip) {
                if (text[i] == '§') {
                    skip = true
                } else {
                    t += text[i]
                }
            } else {
                skip = false
            }
        }
        return t
    }

    fun info(message: String) {
        sendChatMessage("${EnumChatFormatting.GOLD}[${EnumChatFormatting.LIGHT_PURPLE}Duck ${EnumChatFormatting.DARK_PURPLE}Dueller${EnumChatFormatting.GOLD}] ${EnumChatFormatting.WHITE}$message")
    }

    fun error(message: String) {
        sendChatMessage("${EnumChatFormatting.GOLD}[${EnumChatFormatting.LIGHT_PURPLE}Duck ${EnumChatFormatting.DARK_PURPLE}Dueller${EnumChatFormatting.GOLD}] ${EnumChatFormatting.RED}$message")
    }

    fun sendChatMessage(message: String) {
        if (DuckDueller.mc.thePlayer != null) {
            DuckDueller.mc.thePlayer.addChatMessage(ChatComponentText(message))
        }
    }

}