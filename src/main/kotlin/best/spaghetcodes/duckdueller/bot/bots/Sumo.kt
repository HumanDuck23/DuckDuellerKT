package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.*

class Sumo : BotBase("Opponent: ", "Accuracy", "/play duels_sumo_duel") {

    override fun getName(): String {
        return "Sumo"
    }

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        Mouse.startLeftAC()
        Combat.startRandomStrafe(600, 1100)
    }

    override fun onGameEnd() {
        Mouse.stopTracking()
        Mouse.stopLeftAC()
        Movement.clearAll()
        Combat.stopRandomStrafe()
    }

    override fun onAttack() {
        Combat.wTap(80)
        if (combo >= 1) {
            Combat.stopRandomStrafe()
            Movement.clearLeftRight()
        }
    }

    override fun onAttacked() {
        //Combat.shiftTap(150)
    }

    override fun onFoundOpponent() {
        ChatUtils.info("Found opponent: ${opponent?.displayNameString}")
        Mouse.startTracking()
    }

    fun leftEdge(distance: Float): Boolean {
        return (WorldUtils.airOnLeft(mc.thePlayer, distance) && (!Movement.right() && Movement.left()) && combo <= 1)
    }

    fun rightEdge(distance: Float): Boolean {
        return (WorldUtils.airOnRight(mc.thePlayer, distance) && (!Movement.left() && Movement.right()) && combo <= 1)
    }

    override fun onTick() {
        if (isToggled() && gameStarted && opponent != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)

            if (distance > 6) {
                /*val rotations = EntityUtils.getRotations(mc.thePlayer, opponent, true)
                if (rotations != null) {
                    if (rotations[0] < 0) {
                        Movement.stopLeft()
                        Movement.startRight()
                    } else {
                        Movement.stopRight()
                        Movement.startLeft()
                    }
                }*/
                Combat.startRandomStrafe(500, 800)
            } else {
                if (combo < 1 && distance > 2) {
                    Combat.startRandomStrafe(200, 500)
                }
            }

            if (distance <= 2) {
                Combat.stopRandomStrafe()
            }

            if (distance < 1.7) {
                Movement.stopForward()
            } else {
                Movement.startForward()
            }

            // don't walk off an edge
            if (WorldUtils.airInFront(mc.thePlayer, 2f) && mc.thePlayer.onGround) {
                Movement.startSneaking()
            } else {
                Movement.stopSneaking()
            }
            if (WorldUtils.airInBack(mc.thePlayer, 2.5f) && mc.thePlayer.onGround) {
                Movement.startForward()
                Movement.clearLeftRight()
            }
            if (leftEdge(3f) || leftEdge(3f)) {
                Movement.swapLeftRight()
            }
        }
    }

}