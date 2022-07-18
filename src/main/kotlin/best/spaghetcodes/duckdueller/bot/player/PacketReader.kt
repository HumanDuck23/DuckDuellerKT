package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.Config
import best.spaghetcodes.duckdueller.utils.TimeUtils
import best.spaghetcodes.duckdueller.utils.WebHook
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

@ChannelHandler.Sharable
class PacketReader : SimpleChannelInboundHandler<Packet<INetHandler>>(false) {

    @SubscribeEvent
    fun connect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        val pipeline = event.manager.channel().pipeline()
        pipeline.addBefore("packet_handler", this.javaClass.name, this)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Packet<INetHandler>) {
        if (msg is S45PacketTitle && DuckDueller.mc.theWorld != null) {
            TimeUtils.setTimeout(fun () {
                if (msg.message != null) {
                    val unformatted = msg.message.unformattedText.lowercase()
                    if (unformatted.contains("won the duel!") && DuckDueller.mc.thePlayer != null) {
                        var winner = ""
                        var loser = ""
                        var iWon = false

                        val p = ChatUtils.removeFormatting(msg.message.unformattedText).split("won")[0].trim()

                        if (unformatted.contains(DuckDueller.mc.thePlayer.displayNameString.lowercase())) {
                            Session.wins++
                            winner = DuckDueller.mc.thePlayer.displayNameString
                            loser = DuckDueller.getBot()?.lastOpponentName ?: "---"
                            iWon = true
                        } else {
                            Session.losses++
                            ChatUtils.info("Adding $p to the list of players to dodge...")
                            DuckDueller.getBot()?.playersLost?.add(p)
                            winner = p
                            loser = DuckDueller.mc.thePlayer.displayNameString
                            iWon = false
                        }
                        ChatUtils.info(Session.getSession())

                        // Send the webhook embed
                        val fields = WebHook.buildFields(arrayListOf(mapOf("name" to "Winner", "value" to winner, "inline" to "true"), mapOf("name" to "Loser", "value" to loser, "inline" to "true")))
                        val footer = WebHook.buildFooter(ChatUtils.removeFormatting(Session.getSession()), "https://raw.githubusercontent.com/HumanDuck23/upload-stuff-here/main/duck_dueller.png")
                        val author = WebHook.buildAuthor("Duck Dueller", "https://raw.githubusercontent.com/HumanDuck23/upload-stuff-here/main/duck_dueller.png")
                        val thumbnail = WebHook.buildThumbnail("https://raw.githubusercontent.com/HumanDuck23/upload-stuff-here/main/duck_dueller.png")

                        WebHook.sendEmbed(
                            Config.get("webhook").toString(),
                            WebHook.buildEmbed("${if (iWon) ":smirk:" else ":confused:"} Game ${if (iWon) "WON" else "LOST"}!", fields, footer, author, thumbnail, 0xffffff))

                    }
                }
            }, 1000)
        } else if (msg is S3EPacketTeams) {
            Queue.teamPacket(msg)
        }
        ctx.fireChannelRead(msg)
    }

}