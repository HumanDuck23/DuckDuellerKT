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
        Combat.startRandomStrafe(400, 1100)
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

            if (distance > 2) {
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
                Combat.startRandomStrafe(500, 1100)
            }

            if (distance <= 2) {
                Combat.stopRandomStrafe()
            }

            if (distance < 1.7) {
                Movement.stopForward()
            } else {
                Movement.startForward()
            }

            if (opponent != null && opponent is EntityPlayer) {
                if (WorldUtils.airInBack(opponent!!, 2.5f) || WorldUtils.airOnLeft(opponent!!, 2.5f) || WorldUtils.airOnRight(
                        opponent!!, 2.5f)) {
                    Combat.stopRandomStrafe()
                }
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
            if (leftEdge(3f) || rightEdge(3f)) {
                Movement.swapLeftRight()
            }
        }
    }

}