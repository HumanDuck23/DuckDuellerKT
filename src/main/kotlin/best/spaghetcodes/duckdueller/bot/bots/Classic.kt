package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.*
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonObject
import net.minecraft.init.Blocks
import kotlin.math.acos

class Classic : BotBase("Opponent: ", "Accuracy", "/play duels_classic_duel") {

    var shotsFired = 0

    override fun getName(): String {
        return "Classic"
    }

    override fun onOpponentStats(p: JSONDataClasses.Player, stats: JsonObject) {
        if (isToggled() && !gameStarted && stats.get("success").asBoolean) {
            val player = stats.get("player").asJsonObject
            val duels = player.get("stats").asJsonObject.get("Duels").asJsonObject

            var cws = duels.get("current_classic_winstreak")?.asInt
            var wins = duels.get("classic_duel_wins")?.asInt
            var losses = duels.get("classic_duel_losses")?.asInt

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

    override fun beforeStart() {
        LobbyMovement.stop()
    }

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
    }

    override fun onGameEnd() {
        shotsFired = 0
        Mouse.stopLeftAC()
        val i = TimeUtils.setInterval(Mouse::stopLeftAC, 100, 100)
        TimeUtils.setTimeout(fun () {
            i?.cancel()
            Mouse.stopTracking()
            Movement.clearAll()
            Combat.stopRandomStrafe()
        }, RandomUtils.randomIntInRange(200, 400))
    }

    override fun onAttack() {
        //Combat.wTap(100)
        val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)
        if (distance < 3) {
            if (mc.thePlayer != null && mc.thePlayer.heldItem != null) {
                val n = mc.thePlayer.heldItem.displayName.lowercase()
                if (n.contains("rod")) {
                    Combat.wTap(200)
                } else if (n.contains("sword")) {
                    Mouse.rClick(RandomUtils.randomIntInRange(80, 100))
                }
            }
        }
        Movement.clearLeftRight()
    }

    fun opponentLookingAway(): Boolean {
        if (mc.thePlayer != null && opponent != null) {
            val vec1 = EntityUtils.get2dLookVec(mc.thePlayer)
            val vec2 = EntityUtils.get2dLookVec(opponent!!)

            val angle = acos((vec1.xCoord * vec2.xCoord + vec1.yCoord * vec2.yCoord) / (vec1.lengthVector() * vec2.lengthVector()))  * 180 / Math.PI
            return angle in 20f..70f
        }
        return false
    }

    override fun onTick() {
        if (isToggled() && mc.thePlayer != null) {
            if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) != Blocks.air && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(150, 250))
            }
        }
        if (isToggled() && gameStarted && opponent != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)

            if (distance < Config.get("maxDistanceLook") as Int) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }
            if (distance < Config.get("maxDistanceAttack") as Int && !Mouse.isUsingProjectile()) {
                Mouse.startLeftAC()
            } else {
                Mouse.stopLeftAC()
            }

            if (distance > 8.8) {
                Movement.startJumping()
            } else {
                if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) == Blocks.air) {
                    Movement.stopJumping()
                }
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance < 1 || (distance < 2.7 && combo >= 1)) {
                Movement.stopForward()
            } else {
                Movement.startForward()
            }

            if (distance < 1.5 && mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.displayName.lowercase().contains("sword")) {
                Inventory.setInvItem("sword")
                Mouse.rClickUp()
                Mouse.startLeftAC()
            }

            if ((distance in 6.0..6.5 || distance in 8.0..8.5) && !opponentLookingAway()) {
                if (!Mouse.isUsingProjectile()) {
                    Mouse.stopLeftAC()
                    Mouse.setUsingProjectile(true)
                    TimeUtils.setTimeout(fun () {
                        Inventory.setInvItem("rod")
                        TimeUtils.setTimeout(fun () {
                            val r = RandomUtils.randomIntInRange(100, 200)
                            Mouse.rClick(r)
                            TimeUtils.setTimeout(fun () {
                                Mouse.setUsingProjectile(false)
                            }, r + RandomUtils.randomIntInRange(50, 100))
                            TimeUtils.setTimeout(fun () {
                                Inventory.setInvItem("sword")
                                TimeUtils.setTimeout(fun () {
                                    if (gameStarted) {
                                        Mouse.startLeftAC()
                                    }
                                }, RandomUtils.randomIntInRange(100, 150))
                            }, RandomUtils.randomIntInRange(500, 600))
                        }, RandomUtils.randomIntInRange(50, 90))
                    }, RandomUtils.randomIntInRange(10, 30))
                }
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if (opponentLookingAway() && distance in 3.5f..30f) {
                // bruh they running, that's cringe
                if (distance > 5 && !Mouse.isUsingProjectile() && shotsFired < 5) {
                    clear = true
                    Mouse.stopLeftAC()
                    Mouse.setUsingProjectile(true)
                    TimeUtils.setTimeout(fun () {
                        Inventory.setInvItem("bow")
                        TimeUtils.setTimeout(fun () {
                            val r = RandomUtils.randomIntInRange(1000, 1500)
                            Mouse.rClick(r)
                            TimeUtils.setTimeout(fun () {
                                shotsFired++
                                Mouse.setUsingProjectile(false)
                                Inventory.setInvItem("sword")
                                TimeUtils.setTimeout(fun () {
                                    if (gameStarted) {
                                        Mouse.startLeftAC()
                                    }
                                }, RandomUtils.randomIntInRange(100, 200))
                            }, r + RandomUtils.randomIntInRange(100, 150))
                        }, RandomUtils.randomIntInRange(100, 200))
                    }, RandomUtils.randomIntInRange(50, 100))
                } else {
                    clear = false
                    if (opponentMovingLeft()) {
                        movePriority[0] += 1
                    } else {
                        movePriority[1] += 1
                    }
                }
            } else {
                if (distance in 15f..8f) {
                    randomStrafe = true
                } else {
                    randomStrafe = false
                    if (opponent != null && opponent!!.heldItem != null && opponent!!.heldItem.displayName.lowercase().contains("bow")) {
                        randomStrafe = true
                        if (distance < 15) {
                            Movement.stopJumping()
                        }
                    } else {
                        if (combo < 2 && distance < 8) {
                            if (opponentMovingLeft()) {
                                movePriority[1] += 1
                            } else {
                                movePriority[0] += 1
                            }
                        } else {
                            clear = true
                        }
                    }
                }
            }

            if (clear) {
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()
            } else {
                if (randomStrafe) {
                    Combat.startRandomStrafe(1000, 2000)
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
        }
    }
}