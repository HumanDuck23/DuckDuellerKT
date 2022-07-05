package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Queue {

    fun joinGame(msg: String) {
        DuckDueller.mc.thePlayer.sendChatMessage(msg)
    }

    fun leaveGame() {
        DuckDueller.mc.thePlayer.sendChatMessage("/l")
        TimeUtils.setTimeout(fun () {
            DuckDueller.mc.thePlayer.sendChatMessage("/l")
        }, 500)
    }

}