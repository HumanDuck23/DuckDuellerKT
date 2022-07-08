package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object Queue {

    var inGame = false

    fun joinGame(msg: String) {
        DuckDueller.mc.thePlayer.sendChatMessage(msg)
    }

    fun leaveGame() {
        DuckDueller.mc.thePlayer.sendChatMessage("/l")
        TimeUtils.setTimeout(fun () {
            DuckDueller.mc.thePlayer.sendChatMessage("/l")
        }, 500)
    }

    @SubscribeEvent
    fun onChatMessage(ev: ClientChatReceivedEvent) {
        val unformatted = ev.message.unformattedText
        if (unformatted.matches(Regex(".* has joined \\(./2\\)!"))) {
            inGame = true
        }
    }

    @SubscribeEvent
    fun onJoinWorld(ev: EntityJoinWorldEvent) {
        if (DuckDueller.mc.thePlayer != null && ev.entity == DuckDueller.mc.thePlayer) {
            inGame = false
        }
    }

}