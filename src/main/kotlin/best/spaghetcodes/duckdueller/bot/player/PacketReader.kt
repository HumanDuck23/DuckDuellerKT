package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
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
                        println("end of duel title!")
                        if (unformatted.contains(DuckDueller.mc.thePlayer.displayNameString.lowercase())) {
                            Session.wins++
                        } else {
                            Session.losses++
                        }
                        ChatUtils.info(Session.getSession())
                    }
                }
            }, 1000)
        } else if (msg is S3EPacketTeams) {
            Queue.teamPacket(msg)
        }
        ctx.fireChannelRead(msg)
    }

}