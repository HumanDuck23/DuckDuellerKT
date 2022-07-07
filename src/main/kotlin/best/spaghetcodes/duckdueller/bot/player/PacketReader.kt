package best.spaghetcodes.duckdueller.bot.player

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S45PacketTitle
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
        if (msg is S45PacketTitle) {
            // im too tired to implement this rn but it works
        }
        ctx.fireChannelRead(msg)
    }

}