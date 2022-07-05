package best.spaghetcodes.duckdueller.bot.player

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

}