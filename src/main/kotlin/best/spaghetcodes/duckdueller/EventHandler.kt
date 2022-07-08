package best.spaghetcodes.duckdueller

import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.Config
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class EventHandler() {

    @SubscribeEvent
    fun onChat(ev: ClientChatReceivedEvent) {
        if (ev.message.unformattedText.contains("Your new API key is ")) {
            val key = ev.message.unformattedText.split("Your new API key is ")[1]
            Config.set("apiKey", key)
            Config.save()
            ChatUtils.info("Your API key has been saved!")
        }
    }

}