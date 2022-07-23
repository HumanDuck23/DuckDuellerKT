package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import best.spaghetcodes.duckdueller.utils.WorldUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.Timer

object LobbyMovement {

    private var tickYawChange = 0f
    private var intervals: ArrayList<Timer?> = ArrayList()

    fun sumo() {
        sumo1()
    }

    fun stop() {
        Movement.clearAll()
        tickYawChange = 0f
        intervals.forEach { it?.cancel() }
    }

    private fun sumo1() {
        if (DuckDueller.mc.thePlayer != null) {
            val left = RandomUtils.randomBool()

            tickYawChange = if (left) -8.5f else 8.5f
            TimeUtils.setTimeout(fun () {
                Movement.startForward()
                Movement.startSprinting()
                TimeUtils.setTimeout(fun () {
                    Movement.startJumping()
                }, RandomUtils.randomIntInRange(400, 800))
                intervals.add(TimeUtils.setInterval(
                    fun () {
                        tickYawChange = if (WorldUtils.airInFront(DuckDueller.mc.thePlayer, 5f)) {
                            RandomUtils.randomDoubleInRange(if (left) 7.5 else -7.5, if (left) 10.0 else -10.0).toFloat()
                        } else {
                            0f
                        }
                           }, 0, RandomUtils.randomIntInRange(50, 100)))
            }, RandomUtils.randomIntInRange(100, 250))
        }
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (DuckDueller.getBot()?.isToggled() == true && tickYawChange != 0f && DuckDueller.mc.thePlayer != null && DuckDueller.getBot()?.gameStarted == false) {
            DuckDueller.mc.thePlayer.rotationYaw += tickYawChange
        }
    }

}