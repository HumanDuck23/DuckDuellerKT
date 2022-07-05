package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils

object Combat {

    fun wTap(duration: Int) {
        Movement.stopForward()
        TimeUtils.setTimeout(Movement::startForward, duration)
    }

    fun sTap(duration: Int) {
        Movement.startBackward()
        TimeUtils.setTimeout(Movement::stopBackward, duration)
    }

    fun aTap(duration: Int) {
        Movement.startLeft()
        TimeUtils.setTimeout(Movement::stopLeft, duration)
    }

    fun dTap(duration: Int) {
        Movement.startRight()
        TimeUtils.setTimeout(Movement::stopRight, duration)
    }

    fun spamA(hold: Int, duration: Int) {
        val spamTimer = TimeUtils.setInterval(fun () { aTap(hold) }, 0, hold * 2 + RandomUtils.randomIntInRange(0, hold/5))
        TimeUtils.setTimeout(fun () { spamTimer?.cancel() }, duration)
    }

    fun spamD(hold: Int, duration: Int) {
        val spamTimer = TimeUtils.setInterval(fun () { dTap(hold) }, 0, hold * 2 + RandomUtils.randomIntInRange(0, hold/5))
        TimeUtils.setTimeout(fun () { spamTimer?.cancel() }, duration)
    }

}