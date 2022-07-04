package best.spaghetcodes.duckdueller

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent

public const val NAME = "Duck Dueller"
public const val VERSION = "0.1.0"
public const val MOD_ID = "duckdueller"

@Mod(
    modid = MOD_ID,
    name = NAME,
    version = VERSION
)
class DuckDueller {

    @EventHandler
    fun init(event: FMLInitializationEvent) {
        println("Duck Dueller v$VERSION is loading...")
    }

}