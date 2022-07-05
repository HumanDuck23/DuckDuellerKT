package best.spaghetcodes.duckdueller.utils

import java.util.*

object TimeUtils {

    /**
     * Call a function after delay ms
     */
    fun setTimeout(function: () -> Unit, delay: Int) {
        try {
            Timer().schedule(
                object : TimerTask() {
                    override fun run() {
                        function()
                    }
                }, delay.toLong()
            )
        } catch (e: Exception) {
            println("Error scheduling timer with ${delay}ms: " + e.message)
        }
    }

    /**
     * Call a function every interval ms after delay ms
     */
    fun setInterval(function: () -> Unit, delay: Int, interval: Int): Timer? {
        try {
            val timer = Timer()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        function()
                    }
                }, delay.toLong(), interval.toLong()
            )
            return timer
        } catch (e: Exception) {
            println("Error scheduling timer with ${delay}ms delay and ${interval}m interval: " + e.message)
        }
        return null
    }

}