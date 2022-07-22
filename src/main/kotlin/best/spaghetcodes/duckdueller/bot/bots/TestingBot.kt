package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.WorldUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import kotlin.math.roundToInt

class TestingBot : BotBase("Opponent: ", "Accuracy", "/play duels_sumo_duel") {

    override fun getName(): String {
        return "Testing"
    }

    override fun onTick() {
        if (mc.theWorld != null && mc.thePlayer != null) {
            if (WorldUtils.airInFront(mc.thePlayer, 2f)) {
                Movement.stopForward()
            }

            if (WorldUtils.airCheckAngle(mc.thePlayer, 4f, 45f)) {
                Movement.stopLeft()
            }

            if (WorldUtils.airCheckAngle(mc.thePlayer, 4f, -45f)) {
                Movement.stopRight()
            }

            if (WorldUtils.airOnLeft(mc.thePlayer, 4f)) {
                Movement.stopLeft()
            }
            if (WorldUtils.airOnRight(mc.thePlayer, 4f)) {
                Movement.stopRight()
            }
        }
    }

}