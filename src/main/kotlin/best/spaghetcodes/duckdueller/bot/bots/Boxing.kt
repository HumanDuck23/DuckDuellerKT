package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.*
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonObject
import java.util.Timer
import kotlin.math.acos

class Boxing : BotBase("Opponent: ", "Accuracy", "/play duels_boxing_duel") {

    override fun getName(): String {
        return "Boxing"
    }

    private var runner = false
    private var fishTimer: Timer? = null

    fun fishFunc(fish: Boolean = true) {
        if (gameStarted) {
            if (fish) {
                Inventory.setInvItem("fish")
            } else {
                Inventory.setInvItem("sword")
            }
            fishTimer = TimeUtils.setTimeout(fun () {
                fishFunc(!fish)
            }, RandomUtils.randomIntInRange(10000, 20000))
        }
    }

    override fun onOpponentStats(p: JSONDataClasses.Player, stats: JsonObject) {
        if (isToggled() && !gameStarted && stats.get("success").asBoolean) {
            val player = stats.get("player").asJsonObject
            val duels = player.get("stats").asJsonObject.get("Duels").asJsonObject

            var cws = duels.get("current_boxing_winstreak")?.asInt
            var wins = duels.get("boxing_duel_wins")?.asInt
            var losses = duels.get("boxing_duel_losses")?.asInt

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
        TimeUtils.setTimeout(this::fishFunc, RandomUtils.randomIntInRange(10000, 20000))
    }

    override fun onGameEnd() {
        Mouse.stopTracking()
        Mouse.stopLeftAC()
        Movement.clearAll()
        Combat.stopRandomStrafe()
        fishTimer?.cancel()
    }

    override fun onAttack() {
        Combat.wTap(150)
        Movement.clearLeftRight()
    }

    override fun onAttacked() {
        if (opponentCombo > 4) {
            Movement.stopForward()
            Movement.startBackward()
            TimeUtils.setTimeout(fun () {
                Movement.stopBackward()
                Movement.startForward()
            }, RandomUtils.randomIntInRange(1500, 2000))
        }
    }

    fun opponentLookingAway(): Boolean {
        if (mc.thePlayer != null && opponent != null) {
            val vec1 = EntityUtils.get2dLookVec(mc.thePlayer)
            val vec2 = EntityUtils.get2dLookVec(opponent!!)

            val angle = acos((vec1.xCoord * vec2.xCoord + vec1.yCoord * vec2.yCoord) / (vec1.lengthVector() * vec2.lengthVector()))  * 180 / Math.PI
            return angle in 30f..60f
        }
        return false
    }

    override fun onTick() {
        if (isToggled() && gameStarted && opponent != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)

            if (distance < Config.get("maxDistanceLook") as Int) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }
            if (distance < Config.get("maxDistanceAttack") as Int) {
                Mouse.startLeftAC()
            } else {
                Mouse.stopLeftAC()
            }

            if (distance < 1 || (distance < 2.7 && combo >= 2)) {
                Movement.stopForward()
            } else {
                if (!Movement.backward()) {
                    Movement.startForward()
                }
            }

            if (opponentLookingAway() && distance > 3.5) {
                // bruh they running, that's cringe
                if (!runner) {
                    ChatUtils.info("Running away cringe")
                    runner = true
                }
                if (opponentMovingLeft()) {
                    Movement.startLeft()
                    Movement.stopRight()
                } else {
                    Movement.startRight()
                    Movement.stopLeft()
                }
            } else {
                runner = false
                if (distance in 15f..8f) {
                    Combat.startRandomStrafe(400, 800)
                } else {
                    Combat.stopRandomStrafe()
                    if (combo < 2 && distance < 8) {
                        val rotations = EntityUtils.getRotations(opponent, mc.thePlayer, true)
                        if (rotations != null) {
                            if (rotations[0] < 0) {
                                Movement.stopLeft()
                                Movement.startRight()
                            } else {
                                Movement.stopRight()
                                Movement.startLeft()
                            }
                        }
                    } else {
                        Movement.clearLeftRight()
                    }
                }
            }
        }
    }

}