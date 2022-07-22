package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.*
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonObject
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumChatFormatting
import java.math.RoundingMode
import java.text.DecimalFormat

class Sumo : BotBase("Opponent: ", "Accuracy", "/play duels_sumo_duel") {

    override fun getName(): String {
        return "Sumo"
    }

    override fun onOpponentStats(p: JSONDataClasses.Player, stats: JsonObject) {
        if (isToggled() && !gameStarted && stats.get("success").asBoolean) {
            val player = stats.get("player").asJsonObject
            val duels = player.get("stats").asJsonObject.get("Duels").asJsonObject

            var cws = duels.get("current_sumo_winstreak")?.asInt
            var wins = duels.get("sumo_duel_wins")?.asInt
            var losses = duels.get("sumo_duel_losses")?.asInt

            if (cws == null)
                cws = 0
            if (wins == null)
                wins = 0
            if (losses == null)
                losses = 0

            val wlr = wins.toFloat() / (if (losses == 0) 1F else losses.toFloat())

            onParsedStats(p, wins, wlr, cws)
        } else {
            ChatUtils.error("Error reading stats, leaving game...")
            Queue.leaveGame()
        }
    }

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        Mouse.startLeftAC()
        //Combat.startRandomStrafe(400, 1100)
    }

    override fun onGameEnd() {
        Mouse.stopTracking()
        Mouse.stopLeftAC()
        Movement.clearAll()
        Combat.stopRandomStrafe()
    }

    override fun onAttack() {
        println("Attack! Combo: $combo")
        Combat.wTap(80)
        //Combat.stopRandomStrafe()
        Movement.clearLeftRight()
    }

    override fun onAttacked() {
        //Combat.shiftTap(150)
        //Combat.startRandomStrafe(400, 800)
    }

    override fun onFoundOpponent() {
        ChatUtils.info("Found opponent: ${opponent?.displayNameString}")
        Mouse.startTracking()
    }

    fun leftEdge(distance: Float): Boolean {
        return (WorldUtils.airOnLeft(mc.thePlayer, distance))
    }

    fun rightEdge(distance: Float): Boolean {
        return (WorldUtils.airOnRight(mc.thePlayer, distance))
    }

    fun nearEdge(distance: Float): Boolean { // doesnt check front
        return (rightEdge(distance) || leftEdge(distance) || WorldUtils.airInBack(mc.thePlayer, distance))
    }

    fun opponentNearEdge(distance: Float): Boolean {
        return (WorldUtils.airInBack(opponent!!, distance) || WorldUtils.airOnLeft(opponent!!, distance) || WorldUtils.airOnRight(
            opponent!!, distance))
    }

    override fun onTick() {
        if (isToggled() && gameStarted && opponent != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)

            val movePriority = arrayListOf(0, 0)
            var clear = false

            if (WorldUtils.airCheckAngle(mc.thePlayer, 6f, 45f)) {
                movePriority[1] += 5
            } else if (WorldUtils.airCheckAngle(mc.thePlayer, 6f, -45f)) {
                movePriority[0] += 5
            }

            if (nearEdge(3f) && !opponentNearEdge(2f)) {
                if (opponentMovingLeft() && !leftEdge(3f)) {
                    movePriority[0] += 5
                } else if (opponentMovingRight() && !rightEdge(3f)) {
                    movePriority[1] += 5
                }
            } else {
                if (opponentNearEdge(3f) && distance in 0.5f..8f && combo <= 1 && ticksSinceLastDamage > 40) {
                    // Opponent is at the edge, lets mirror their movement to not let them get out of the corner
                    if (opponentMovingLeft() && !rightEdge(2f)) {
                        movePriority[1] += 1
                    } else if (opponentMovingRight() && !leftEdge(2f)) {
                        movePriority[0] += 1
                    } else {
                        clear = true
                    }
                } else {
                    val rotations = EntityUtils.getRotations(mc.thePlayer, opponent, true)
                    if (rotations != null) {
                        if (rotations[0] < 0) {
                            movePriority[1] += 1
                        } else {
                            movePriority[0] += 1
                        }
                    }
                }
            }

            if (combo >= 1) {
                clear = true
                if (combo >= 2 && distance >= 3.2 && mc.thePlayer.onGround && !nearEdge(3f)) {
                    Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
                }
            }

            if (clear) {
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()
            } else {
                if (movePriority[0] > movePriority[1]) {
                    Movement.stopRight()
                    Movement.startLeft()
                } else if (movePriority[1] > movePriority[0]) {
                    Movement.stopLeft()
                    Movement.startRight()
                } else {
                    if (RandomUtils.randomBool()) {
                        Movement.startLeft()
                    } else {
                        Movement.startRight()
                    }
                }
            }

            if (distance < 1) {
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
        }
    }

}