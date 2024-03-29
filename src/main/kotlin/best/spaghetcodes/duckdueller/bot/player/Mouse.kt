package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.Config
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs

object Mouse {

    private var leftAC = false
    private var rClickDown = false

    private var tracking = false

    private var _usingProjectile = false

    fun leftClick() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            KeyBinding.onTick(DuckDueller.mc.gameSettings.keyBindAttack.keyCode)
        }
    }

    fun rClick(duration: Int) {
        if (DuckDueller.getBot()?.isToggled() == true) {
            if (!rClickDown) {
                rClickDown()
                TimeUtils.setTimeout(this::rClickUp, duration)
            }
        }
    }

    fun startLeftAC(delay: Int = 0) {
        if (DuckDueller.getBot()?.isToggled() == true) {
            if (!leftAC) {
                leftAC = true
                TimeUtils.setTimeout(this::leftACFunc, delay)
            }
        }
    }

    fun stopLeftAC() {
        // no need to check for toggled state here
        leftAC = false
    }

    fun startTracking() {
        tracking = true
    }

    fun stopTracking() {
        tracking = false
    }

    fun setUsingProjectile(proj: Boolean) {
        _usingProjectile = proj
    }

    fun isUsingProjectile(): Boolean {
        return _usingProjectile
    }

    private fun leftACFunc() {
        if (DuckDueller.getBot()?.isToggled() == true && leftAC) {
            val minCPS = Config.get("minCPS") as Int
            val maxCPS = Config.get("maxCPS") as Int

            val cps = RandomUtils.randomIntInRange(minCPS, maxCPS)

            // this could be the whole ac method, BUT the cps should jitter between min and max
            val cpsTimer = TimeUtils.setInterval(fun () {
                val minDelay = 1000 / cps / 3
                val maxDelay = minDelay * 2
                val delay = RandomUtils.randomIntInRange(minDelay, maxDelay)
                TimeUtils.setTimeout(this::leftClick, delay)
            }, 0, 1000/cps)

            TimeUtils.setTimeout(fun () {
                cpsTimer?.cancel()
                leftACFunc()
            }, RandomUtils.randomIntInRange(1500, 2000)) // run this method every 1.5-2 seconds
        }
    }

    private fun rClickDown() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            rClickDown = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    fun rClickUp() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            rClickDown = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, false)
        }
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (DuckDueller.mc.thePlayer != null && DuckDueller.getBot()?.isToggled() == true && tracking && DuckDueller.getBot()?.getOpponentE() != null) {
            val rotations = EntityUtils.getRotations(DuckDueller.mc.thePlayer, DuckDueller.getBot()?.getOpponentE(), false)

            if (rotations != null) {
                val lookRand = (Config.get("lookRand") as Float).toDouble()
                var da = ((rotations[0] - DuckDueller.mc.thePlayer.rotationYaw) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()
                val maxRot = Config.get("lookSpeed") as Float
                if (abs(da) > maxRot) {
                    da = if (da > 0) {
                        maxRot
                    } else {
                        -maxRot
                    }
                }

                DuckDueller.mc.thePlayer.rotationYaw += da
                DuckDueller.mc.thePlayer.rotationPitch = rotations[1] // pitch is perfect screw you
            }
        }
    }

}