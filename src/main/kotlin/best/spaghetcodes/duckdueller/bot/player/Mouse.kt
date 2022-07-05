package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.Config
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding

object Mouse {

    private var leftAC = false
    private var rClickDown = false

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
            }, RandomUtils.randomIntInRange(900, 1400)) // run this method every 0.9-1.4 seconds
        }
    }

    private fun rClickDown() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            rClickDown = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    private fun rClickUp() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            rClickDown = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, false)
        }
    }

}