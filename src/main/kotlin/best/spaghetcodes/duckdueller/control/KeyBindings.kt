package best.spaghetcodes.duckdueller.control

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.input.Keyboard

object KeyBindings {

    val keyBindings: ArrayList<KeyBinding> = ArrayList()

    private val toggleBotKeyBinding: KeyBinding = KeyBinding("duck.toggleBot", Keyboard.KEY_SEMICOLON, "category.duck")

    fun registerKeyBindings() {
        println("Registering key bindings")

        keyBindings.add(toggleBotKeyBinding)

        for (keyBinding in keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding)
        }
    }
}