package best.spaghetcodes.duckdueller

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

    private val eventHandler: EventHandler = EventHandler()

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        println("Duck Dueller v$VERSION is loading...")

        KeyBindings.registerKeyBindings()
    }

}