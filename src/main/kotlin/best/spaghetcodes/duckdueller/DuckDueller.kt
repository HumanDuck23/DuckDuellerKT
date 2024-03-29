package best.spaghetcodes.duckdueller

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.bots.Boxing
import best.spaghetcodes.duckdueller.bot.bots.Classic
import best.spaghetcodes.duckdueller.bot.bots.Sumo
import best.spaghetcodes.duckdueller.bot.bots.TestingBot
import best.spaghetcodes.duckdueller.bot.hud.HudRenderer
import best.spaghetcodes.duckdueller.bot.player.LobbyMovement
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.PacketReader
import best.spaghetcodes.duckdueller.bot.player.Queue
import best.spaghetcodes.duckdueller.control.Commands
import best.spaghetcodes.duckdueller.control.KeyBindings
import best.spaghetcodes.duckdueller.utils.Config
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent

const val NAME = "Duck Dueller"
const val VERSION = "0.1.0"
const val MOD_ID = "assets/lang/duckdueller"

@Mod(
    modid = MOD_ID,
    name = NAME,
    version = VERSION,
    modLanguageAdapter = "best.spaghetcodes.duckdueller.adapter.KotlinLanguageAdapter"
)
object DuckDueller {

    val mc: Minecraft = Minecraft.getMinecraft()
    private val eventHandler: EventHandler = EventHandler()

    private var bot: BotBase? = null
    val bots = mapOf("sumo" to Sumo(), "testing" to TestingBot(), "boxing" to Boxing(), "classic" to Classic())

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        println("Duck Dueller v$VERSION is loading...")

        KeyBindings.registerKeyBindings()
        Commands.registerCommands()
        Config.load()

        MinecraftForge.EVENT_BUS.register(eventHandler)
        MinecraftForge.EVENT_BUS.register(Queue)
        MinecraftForge.EVENT_BUS.register(Mouse)
        MinecraftForge.EVENT_BUS.register(LobbyMovement)
        MinecraftForge.EVENT_BUS.register(HudRenderer)
        MinecraftForge.EVENT_BUS.register(PacketReader())

        if (bots.keys.contains(Config.get("currentBot"))) {
            bots[Config.get("currentBot")]?.let { setBot(it) }
        } else {
            setBot(Sumo()) // default bot is sumo
        }
    }

    fun getBot(): BotBase? {
        return bot
    }

    fun setBot(newBot: BotBase) {
        if (bot != null) {
            MinecraftForge.EVENT_BUS.unregister(bot) // make sure to de-register the bot
        }
        bot = newBot
        MinecraftForge.EVENT_BUS.register(bot) // and register the new bot
    }

}