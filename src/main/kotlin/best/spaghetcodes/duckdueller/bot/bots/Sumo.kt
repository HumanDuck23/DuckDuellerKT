package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.*
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonObject

class Sumo : BotBase("Opponent: ", "Accuracy", "/play duels_sumo_duel") {

    override fun getName(): String {
        return "Sumo"
    }

    private var tapping = false

    override fun onJoin() {
        TimeUtils.setTimeout(fun () {
            ChatUtils.info("Starting Lobby Movement")
            LobbyMovement.sumo()
        }, RandomUtils.randomIntInRange(1000, 2000))
    }

    override fun beforeStart() {
        ChatUtils.info("Stopping Lobby Movement")
        LobbyMovement.stop()
    }

    override fun beforeLeave() {
        LobbyMovement.stop()
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
        Combat.startRandomStrafe(600, 1100)
    }

    override fun onGameEnd() {
        Mouse.stopTracking()
        Mouse.stopLeftAC()
        Movement.clearAll()
        Combat.stopRandomStrafe()
    }

    override fun onAttack() {
        tapping = true
        ChatUtils.info("W-Tap")
        Combat.wTap(80)
        TimeUtils.setTimeout(fun () {
            tapping = false
        }, 80)
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

            if (distance > 4.3) {
                Mouse.stopLeftAC()
            } else {
                Mouse.startLeftAC()
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance > 2) {
                if (nearEdge(3f)) {
                    if (opponentMovingRight()) {
                        movePriority[1] += 2
                    } else if (opponentMovingLeft()) {
                        movePriority[0] += 2
                    }
                } else {
                    if (opponentNearEdge(4f) && combo < 1) {
                        if (opponentMovingRight()) {
                            movePriority[0] += 2
                        } else if (opponentMovingLeft() && combo < 1) {
                            movePriority[1] += 2
                        }
                    } else {
                        randomStrafe = true
                    }
                }
            } else {
                clear = true
            }

            // a bunch of if's to detect edges and avoid them instead of just not walking off

            if (
                (WorldUtils.airCheckAngle(mc.thePlayer, 9f, 20f, 60f)
                || WorldUtils.airCheckAngle(mc.thePlayer, 8.5f, 70f, 110f)
                || WorldUtils.airCheckAngle(mc.thePlayer, 9f, 120f, 160f))
                && combo <= 3
            ) {
                movePriority[1] += 5
                clear = false
            }

            if (
                (WorldUtils.airCheckAngle(mc.thePlayer, 9f, -20f, -60f)
                || WorldUtils.airCheckAngle(mc.thePlayer, 8.5f, -70f, -110f)
                || WorldUtils.airCheckAngle(mc.thePlayer, 9f, -120f, -160f))
                && combo <= 3
            ) {
                movePriority[0] += 5
                clear = false
            }

            // placing this here so that it only strafes in a combo if it's REALLY close to an edge
            if (combo >= 1) {
                clear = true
            }

            if (rightEdge(5f)) {
                movePriority[0] += 10
                clear = false
            }
            if (leftEdge(5f)) {
                movePriority[1] += 10
                clear = false
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround && !nearEdge(5f) && !WorldUtils.airInFront(mc.thePlayer, 3f)) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if (clear) {
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()
            } else {
                if (randomStrafe) {
                    Combat.startRandomStrafe(600, 900)
                } else {
                    Combat.stopRandomStrafe()
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
            }

            if (distance < 1.2 || (distance < 2 && opponentNearEdge(3f))) {
                Movement.stopForward()
            } else {
                if (!tapping) {
                    Movement.startForward()
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
        }
    }

}