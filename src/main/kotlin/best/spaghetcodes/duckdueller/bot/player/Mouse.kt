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
    private var changingYawPositive = false
    private var changedYaw = -1
    private var changedYawMax = -1
    private var changeYawBy = 1

    // incompetent dev slaying
    private var changingPitchPositive = false
    private var changedPitch = -1
    private var changedPitchMax = -1
    private var changePitchBy = 1

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

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (DuckDueller.mc.thePlayer != null && DuckDueller.getBot()?.isToggled() == true && tracking && DuckDueller.getBot()?.getOpponentE() != null) {
            val rotations = EntityUtils.getRotations(DuckDueller.mc.thePlayer, DuckDueller.getBot()?.getOpponentE(), false)

            if (rotations != null) { // very stupid mouse jitter code dont bully me
                /*if (changedYaw == -1 && !changingYawPositive) {
                    changedYawMax = RandomUtils.randomIntInRange(-2, 2)
                    changeYawBy = if (changedYawMax > 0) 1 else -1
                    changedYaw = 0
                    changingYawPositive = true
                } else if (changingYawPositive) {
                    changedYaw += changeYawBy
                    if (abs(changedYaw) >= abs(changedYawMax)) {
                        changingYawPositive = false
                    }
                } else {
                    changedYaw -= changeYawBy
                }

                // jos is contributing :OOOOOOO (no he isnt)
                if (changedPitch == -1 && !changingPitchPositive) {
                    changedPitchMax = RandomUtils.randomIntInRange(-1, 1)
                    changePitchBy = if (changedPitchMax > 0) 1 else -1
                    changedPitch = 0
                    changingPitchPositive = true
                } else if (changingPitchPositive) {
                    changedPitch += changePitchBy
                    if (abs(changedYaw) >= abs(changedPitchMax)) {
                        changingPitchPositive = false
                    }
                } else {
                    changedPitch -= changePitchBy
                }*/

                DuckDueller.mc.thePlayer.rotationYaw = rotations[0]// + changedYaw
                DuckDueller.mc.thePlayer.rotationPitch = rotations[1]// + changedPitch // pitch is perfect screw you
            }
        }
    }

}