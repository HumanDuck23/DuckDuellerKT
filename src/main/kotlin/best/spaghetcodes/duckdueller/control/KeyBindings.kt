package best.spaghetcodes.duckdueller.control

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.input.Keyboard

object KeyBindings {

    val keyBindings: ArrayList<KeyBinding> = ArrayList()

    val toggleBotKeyBinding: KeyBinding = KeyBinding("duck.toggleBot", Keyboard.KEY_SEMICOLON, "category.duck")

    fun registerKeyBindings() {
        println("Registering KeyBindings")

        keyBindings.add(toggleBotKeyBinding)

        for (keyBinding in keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding)
        }
    }
}