package best.spaghetcodes.duckdueller.bot

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.control.KeyBindings
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.Config
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.*

/**
 * Base class for all bots
 *
 * Simply extend this class and override some methods
 */
open class BotBase protected constructor(val startMessage: String, val stopMessage: String, val queueCommand: String) {

    protected val mc = Minecraft.getMinecraft();
    protected var opponent: EntityPlayer? = null

    protected var toggled = false

    private var calledFoundOpponent = false
    private var opponentTimer: Timer? = null

    private var gameStarted = false

    private var ticksSinceLastHit = 0

    protected var combo = 0
    protected var opponentCombo = 0

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

    // Public base methods for every bot

    fun toggle() {
        toggled = !toggled
    }

    fun isToggled() = toggled

    fun getOpponentE() = opponent

    // Private backend bot stuff

    private fun _gameStart() {
        gameStarted = true
        val quickRefreshTimer: Timer? = TimeUtils.setInterval(this::bakery, 200, 50)
        TimeUtils.setTimeout(fun () {
            quickRefreshTimer?.cancel()
            opponentTimer = TimeUtils.setInterval(this::bakery, 0, 5000)
        }, 2000)

        onGameStart()
    }

    private fun bakery() { // yes.
        // yes this is a feature
        if (gameStarted) {
            val foundOpponent = getOpponentEntity()
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

    private fun _gameEnd() {
        opponent = null
        calledFoundOpponent = false
        opponentTimer?.cancel()
        gameStarted = false

        onGameEnd()

        TimeUtils.setTimeout(fun () {
            mc.thePlayer.sendChatMessage(Config.get("ggMessage") as String? ?: "GG")
            TimeUtils.setTimeout(fun () {
                best.spaghetcodes.duckdueller.bot.player.Queue.joinGame(queueCommand)
            }, Config.get("rqDelay") as Int)
        }, Config.get("ggDelay") as Int)
    }

    @SubscribeEvent
    fun onChatMessage(ev: ClientChatReceivedEvent) {
        if (isToggled()) {
            val unformatted = ev.message.unformattedText

            if (unformatted.contains(startMessage)) {
                _gameStart()
            } else if (unformatted.contains(stopMessage)) {
                _gameEnd()
            }
        }
    }

    @SubscribeEvent
    fun onTickEvent(ev: ClientTickEvent) {
        onTick()
        ticksSinceLastHit++

        if (KeyBindings.toggleBotKeyBinding.isPressed) {
            toggle()
            ChatUtils.info("Duck Dueller has been toggled ${if (isToggled()) "${EnumChatFormatting.GREEN}on" else "${EnumChatFormatting.RED}off"}")
            if (isToggled()) ChatUtils.info("Current selected bot: ${EnumChatFormatting.BOLD}${EnumChatFormatting.GREEN}${getName()}${EnumChatFormatting.RESET}")
        }

        if (isToggled() && mc.thePlayer != null && mc.thePlayer.maxHurtTime > 0 && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime) {
            combo = 0
            opponentCombo++
            onAttacked()
        }

        if (isToggled() && mc.thePlayer != null && opponent != null && mc.thePlayer.getDistanceToEntity(opponent) > 4 && combo > 0) {
            combo = 0
            ChatUtils.info("combo reset")
        }
    }

    @SubscribeEvent
    fun onAttackEntityEvent(ev: AttackEntityEvent) {
        if (isToggled() && ev.entity === mc.thePlayer && ticksSinceLastHit > 15) {
            opponentCombo = 0
            combo++
            onAttack()
            ticksSinceLastHit = 0
        }
    }

}