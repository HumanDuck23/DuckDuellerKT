package best.spaghetcodes.duckdueller.bot

import best.spaghetcodes.duckdueller.bot.player.JSONDataClasses
import best.spaghetcodes.duckdueller.control.KeyBindings
import best.spaghetcodes.duckdueller.bot.player.Queue
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.acos

/**
 * Base class for all bots
 *
 * Simply extend this class and override some methods
 */
open class BotBase protected constructor(val startMessage: String, val stopMessage: String, val queueCommand: String, val stopQuickRefresh: Int = 10000) {

    protected val mc = Minecraft.getMinecraft();
    protected var opponent: EntityPlayer? = null

    protected var toggled = false

    private var calledFoundOpponent = false
    private var opponentTimer: Timer? = null

    var gameStarted = false

    protected var ticksSinceLastHit = 0
    protected var ticksSinceLastDamage = 0

    var opponentPositions = ArrayList<Vec3>() // tracks opponent position back 20 ticks

    protected var combo = 0
    protected var opponentCombo = 0

    var playersSent = ArrayList<String>()
    var playersLost = ArrayList<String>()
    var gotStats = false

    var lastOpponentName = ""

    private var calledJoin = false

    private var attackedID = -1

    // These need to be overridden by subclasses to customize the bots behavior
    open fun getName(): String {
        return "Base"
    }

    /**
     * Called when the bot attacks a player
     */
    protected open fun onAttack() {}

    /**
     * Called when the bot is attacked
     */
    protected open fun onAttacked() {}

    /**
     * Called when the game starts
     */
    protected open fun onGameStart() {}

    /**
     * Called when the game ends
     */
    protected open fun onGameEnd() {}

    /**
     * Called when the opponent entity is found
     */
    protected open fun onFoundOpponent() {}

    /**
     * Called every tick
     */
    protected open fun onTick() {}

    /** Called when the bot joins the game **/
    protected open fun onJoin() {}

    /** Called right before the game starts (1s) **/
    protected open fun beforeStart() {}

    /** Called before the bot leaves the game (dodge **/
    protected open fun beforeLeave() {}

    /**
     * Called when the opponent's stats have been fetched
     */
    open fun onOpponentStats(p: JSONDataClasses.Player, stats: JsonObject) {}

    // Public base methods for every bot

    fun toggle() {
        toggled = !toggled
    }

    fun isToggled() = toggled

    fun getOpponentE() = opponent

    // other

    fun onEntityStatus(packet: S19PacketEntityStatus) {
        if (packet.opCode.toInt() == 2) { // damage sound
            val entity = packet.getEntity(mc.theWorld)
            if (entity != null) {
                if (entity.entityId == attackedID) {
                    attackedID = -1
                    opponentCombo = 0
                    combo++
                    onAttack()
                    ticksSinceLastHit = 0
                } else if (mc.thePlayer != null && entity.entityId == mc.thePlayer.entityId) {
                    ticksSinceLastDamage = 0
                    combo = 0
                    opponentCombo++
                    onAttacked()
                }
            }
        }
    }

    protected fun onParsedStats(p: JSONDataClasses.Player, w: Int, wlr: Float, cws: Int) {
        var dodge = false

        if (playersLost.contains(p.name) && Config.get("dodgeLost") as Boolean) {
            ChatUtils.info("Lost to ${p.name} before, dodging...")
            dodge = true
        }

        gotStats = true

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        val wlrString = df.format(wlr)

        ChatUtils.info("${p.name} ${EnumChatFormatting.GOLD}>> ${EnumChatFormatting.GREEN}W: $w ${EnumChatFormatting.RED}W/L: $wlrString ${EnumChatFormatting.LIGHT_PURPLE}CWS: $cws${EnumChatFormatting.RESET}")

        val dodgeWins = Config.get("dodgeWins") as Int
        val dodgeWS = Config.get("dodgeWS") as Int
        val dodgeWLR = Config.get("dodgeWLR") as Float

        if (w >= dodgeWins) {
            ChatUtils.info("${p.name} has more than $dodgeWins wins, dodging...")
            dodge = true
        } else if (cws >= dodgeWS) {
            ChatUtils.info("${p.name} has a ws higher than $dodgeWS, dodging...")
            dodge = true
        } else if (wlr >= dodgeWLR) {
            ChatUtils.info("${p.name} has a W/L higher than $dodgeWLR, dodging...")
            dodge = true
        }

        playersSent.add(0, p.name)

        if (dodge) {
            beforeLeave()
            resetVars()
            Queue.leaveGame()
            TimeUtils.setTimeout(fun () { Queue.joinGame(queueCommand) }, RandomUtils.randomIntInRange(4000, 8000))
        }
    }

    protected fun opponentMovingRight(): Boolean {
        val angle = calculateAngleRel()
        if (angle != null && angle < 90) {
            return true
        }
        return false
    }

    protected fun opponentMovingLeft(): Boolean {
        val angle = calculateAngleRel()
        if (angle != null && angle > 90) {
            return true
        }
        return false
    }

    private fun calculateAngleRel(): Double? {
        if (mc.thePlayer != null && opponentPositions.size > 1) {
            val _leftVec = mc.thePlayer.lookVec.rotateYaw(90f)
            val leftVec = Vec3(_leftVec.xCoord, 0.0, _leftVec.zCoord)

            val pos1 = if (opponentPositions.size > 1) opponentPositions[1] else opponentPositions[opponentPositions.size - 1]
            val pos2 = opponentPositions[0]
            val opponentVec = Vec3(pos2.xCoord - pos1.xCoord, 0.0, pos2.zCoord - pos1.zCoord)

            val angle = acos((leftVec.xCoord * opponentVec.xCoord + leftVec.zCoord * opponentVec.zCoord) / (leftVec.lengthVector() * opponentVec.lengthVector())) * 180 / Math.PI
            return angle
        } else {
            return null
        }
    }

    // Private backend bot stuff

    private fun _gameStart() {
        gameStarted = true
        val quickRefreshTimer: Timer? = TimeUtils.setInterval(this::bakery, 200, 50)
        TimeUtils.setTimeout(fun () {
            quickRefreshTimer?.cancel()
            opponentTimer = TimeUtils.setInterval(this::bakery, 0, 5000)
        }, stopQuickRefresh)

        onGameStart()
    }

    private fun bakery() { // yes.
        // yes this is a feature
        if (gameStarted) {
            val foundOpponent = getOpponentEntity()
            if (foundOpponent) {
                lastOpponentName = opponent?.displayNameString ?: ""
            }
            if (foundOpponent && !calledFoundOpponent) {
                calledFoundOpponent = true
                onFoundOpponent()
            }
        }
    }

    private fun getOpponentEntity(): Boolean {
        opponent = EntityUtils.getOpponentEntity()
        return opponent != null
    }

    private fun resetVars() {
        opponent = null
        calledFoundOpponent = false
        opponentTimer?.cancel()
        gameStarted = false
        gotStats = false
        calledJoin = false
    }

    private fun _gameEnd() {
        resetVars()

        onGameEnd()

        TimeUtils.setTimeout(fun () {
            mc.thePlayer.sendChatMessage(Config.get("ggMessage") as String? ?: "GG")
            TimeUtils.setTimeout(fun () {
                Queue.joinGame(queueCommand)
                playersSent.removeAll { true }
            }, Config.get("rqDelay") as Int)
        }, Config.get("ggDelay") as Int)
    }

    @SubscribeEvent
    fun onChatMessage(ev: ClientChatReceivedEvent) {
        if (isToggled()) {
            val unformatted = ev.message.unformattedText

            if (unformatted.contains("The game starts in 2 seconds!") && !gotStats && Config.get("dodgeNoStats") as Boolean) {
                ChatUtils.info("Didn't find any stats, leaving game...")
                beforeLeave()
                resetVars()
                Queue.leaveGame()
                TimeUtils.setTimeout(fun() { Queue.joinGame(queueCommand) }, RandomUtils.randomIntInRange(4000, 8000))
            }

            if (unformatted.contains(startMessage)) {
                _gameStart()
            } else if (unformatted.contains(stopMessage)) {
                _gameEnd()
            }

            if (unformatted.matches(Regex(".* has joined \\(./2\\)!")) && !calledJoin) {
                calledJoin = true
                onJoin()
            }

            if (unformatted.contains("The game starts in 1 second!")) {
                beforeStart()
            }
        }
    }

    @SubscribeEvent
    fun onTickEvent(ev: ClientTickEvent) {
        onTick()
        ticksSinceLastHit++
        ticksSinceLastDamage++

        if (KeyBindings.toggleBotKeyBinding.isPressed) {
            toggle()
            ChatUtils.info("Duck Dueller has been toggled ${if (isToggled()) "${EnumChatFormatting.GREEN}on" else "${EnumChatFormatting.RED}off"}")
            if (isToggled()) {
                ChatUtils.info("Current selected bot: ${EnumChatFormatting.BOLD}${EnumChatFormatting.GREEN}${getName()}${EnumChatFormatting.RESET}")
                ChatUtils.info("Joining game...")
                Queue.joinGame(queueCommand)
            }
        }

        /*if (isToggled() && mc.thePlayer != null && mc.thePlayer.maxHurtTime > 0 && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime) {

        }*/

        if (isToggled() && mc.thePlayer != null && opponent != null && (ticksSinceLastHit > 100 || EntityUtils.getDistanceNoY(mc.thePlayer, opponent) > 6) && combo > 0) {
            combo = 0
            ChatUtils.info("combo reset")
        }

        if (isToggled() && opponent != null) {
            if (opponentPositions.size >= 20) { // should never be larger than 20 but ok
                opponentPositions.removeAt(opponentPositions.size - 1)
            }
            opponentPositions.add(0, opponent!!.positionVector)
        }
    }

    @SubscribeEvent
    fun onAttackEntityEvent(ev: AttackEntityEvent) {
        if (isToggled() && ev.entity === mc.thePlayer) {
            attackedID = ev.target.entityId
        }
    }

}