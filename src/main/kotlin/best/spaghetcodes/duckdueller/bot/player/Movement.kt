package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding

object Movement {
    private var forward = false
    private var backward = false
    private var left = false
    private var right = false
    private var jumping = false
    private var sprinting = false
    private var sneaking = false

    fun startForward() {
        if (DuckDueller.getBot()?.isToggled() == true) { // need to do this because the type is Boolean? so it could be null
            forward = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindForward.keyCode, true)
        }
    }

    fun stopForward() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            forward = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindForward.keyCode, false)
        }
    }

    fun startBackward() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            backward = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindBack.keyCode, true)
        }
    }

    fun stopBackward() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            backward = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindBack.keyCode, false)
        }
    }

    fun startLeft() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            left = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindLeft.keyCode, true)
        }
    }

    fun stopLeft() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            left = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindLeft.keyCode, false)
        }
    }

    fun startRight() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            right = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindRight.keyCode, true)
        }
    }

    fun stopRight() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            right = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindRight.keyCode, false)
        }
    }

    fun startJumping() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            jumping = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindJump.keyCode, true)
        }
    }

    fun stopJumping() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            jumping = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindJump.keyCode, false)
        }
    }

    fun startSprinting() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            sprinting = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSprint.keyCode, true)
        }
    }

    fun stopSprinting() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            sprinting = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSprint.keyCode, false)
        }
    }

    fun startSneaking() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            sneaking = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSneak.keyCode, true)
        }
    }

    fun stopSneaking() {
        if (DuckDueller.getBot()?.isToggled() == true) {
            sneaking = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSneak.keyCode, false)
        }
    }

    fun singleJump(holdDuration: Int) {
        startJumping()
        TimeUtils.setTimeout(this::stopJumping, holdDuration)
    }

    fun clearAll() {
        stopForward()
        stopBackward()
        stopLeft()
        stopRight()
        stopJumping()
        stopSprinting()
        stopSneaking()
    }

    fun clearLeftRight() {
        stopLeft()
        stopRight()
    }

    fun swapLeftRight() {
        if (left) {
            stopLeft()
            startRight()
        } else if (right) {
            stopRight()
            startLeft()
        }
    }

    fun forward(): Boolean {
        return forward
    }

    fun backward(): Boolean {
        return backward
    }

    fun left(): Boolean {
        return left
    }

    fun right(): Boolean {
        return right
    }

    fun jumping(): Boolean {
        return jumping
    }

    fun sprinting(): Boolean {
        return sprinting
    }

    fun sneaking(): Boolean {
        return sneaking
    }

}