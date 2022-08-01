package best.spaghetcodes.duckdueller.bot.hud

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.player.Session
import best.spaghetcodes.duckdueller.utils.Config
import com.sun.org.apache.xpath.internal.operations.Bool
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent
import java.awt.Color
import kotlin.math.roundToInt

object HudRenderer {

    private val mc = Minecraft.getMinecraft()

    @SubscribeEvent
    fun onRenderTick(ev: RenderTickEvent) {
        if (DuckDueller.getBot()?.isToggled() == true && Config.get("showHud") as Boolean) {
            val str1 = "${EnumChatFormatting.GREEN}Wins: ${Session.wins}"
            val str2 = "${EnumChatFormatting.RED}Losses: ${Session.losses}"

            val wlr = Session.wins.toFloat() / if (Session.losses == 0) 1f else Session.losses.toFloat()
            val str3 = "${EnumChatFormatting.LIGHT_PURPLE}W/L: ${(wlr * 100).roundToInt() / 100f}"

            val fr = mc.fontRendererObj

            val width = if (fr.getStringWidth(str1) > fr.getStringWidth(str2)) fr.getStringWidth(str1) else fr.getStringWidth(str2)
            val height = fr.FONT_HEIGHT * 3

            GlStateManager.pushMatrix()
            GlStateManager.scale(0.7, 0.7, 0.7)

            Gui.drawRect(10, 10, 10 + width + 10, 10 + height + 10, Color(0, 0, 0, 210).rgb)
            fr.drawString(str1, 15f, 15f, -1, true)
            fr.drawString(str2, 15f, 15f + fr.FONT_HEIGHT, -1, true)
            fr.drawString(str3, 15f, 15f + fr.FONT_HEIGHT * 2, -1, true)

            GlStateManager.popMatrix()
        }
    }

}