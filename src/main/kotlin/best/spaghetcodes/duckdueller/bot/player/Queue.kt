package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.Config
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object Queue {

    var inGame = false

    fun joinGame(msg: String) {
        if (!DuckDueller.getBot()?.gameStarted!!)
            DuckDueller.mc.thePlayer.sendChatMessage(msg)
    }

    fun leaveGame() {
        if (!DuckDueller.getBot()?.gameStarted!!) {
            DuckDueller.mc.thePlayer.sendChatMessage("/l")
            TimeUtils.setTimeout(fun () {
                DuckDueller.mc.thePlayer.sendChatMessage("/l")
            }, 500)
        }
    }

    @SubscribeEvent
    fun onChatMessage(ev: ClientChatReceivedEvent) {
        val unformatted = ev.message.unformattedText
        if (unformatted.matches(Regex(".* has joined \\(./2\\)!"))) {
            inGame = true
        } else if (unformatted.lowercase().contains("something went wrong trying") || unformatted.lowercase().contains("please don't spam the command")) {
            TimeUtils.setTimeout(fun () {
                if (DuckDueller.getBot()?.queueCommand != "")
                    joinGame(DuckDueller.getBot()?.queueCommand!!)
            }, RandomUtils.randomIntInRange(6000, 8000))
        }
    }

    @SubscribeEvent
    fun onJoinWorld(ev: EntityJoinWorldEvent) {
        if (DuckDueller.mc.thePlayer != null && ev.entity == DuckDueller.mc.thePlayer) {
            inGame = false
        }
    }

    @SubscribeEvent
    fun onDisconnect(ev: ClientDisconnectionFromServerEvent) {
        inGame = false
    }

    fun teamPacket(packet: S3EPacketTeams) {
        if (packet.func_149307_h() == 3) { // mode 3 is for adding entities to a team
            val entities = packet.func_149310_g() // this is the entity list
            for (entity in entities) {
                if (entity.length > 2) { // hypixel sends fake entities with the names just being an emoji (??? why lol)
                    // small delay for the inGame state to update
                    TimeUtils.setTimeout(fun() { if (inGame) { checkPlayer(entity) } }, 150)
                }
            }
        }
    }

    private fun checkPlayer(player: String) {
        // https://api.mojang.com/users/profiles/minecraft/<player>
        runBlocking {
            launch {
                val res = unameToUUID(player)
                if (res != "") { // api returns 204 no content for invalid players
                    println("Deserializing $res")
                    val gson = Gson()
                    val p = gson.fromJson(res, JSONDataClasses.Player::class.java)
                    if (DuckDueller.mc.thePlayer != null && p.name != DuckDueller.mc.thePlayer.displayNameString) {
                        if (!DuckDueller.getBot()?.playersSent?.contains(p.name)!!) { // sometimes players get sent multiple times, dont send that many requests
                            val stats = getHypixelStats(p.id)
                            println("Got stats for ${p.name}")
                            if (stats != null) {
                                println("Calling onOpponentStats")
                                DuckDueller.getBot()?.onOpponentStats(p, stats)
                            } else {
                                ChatUtils.error("Failed to get stats for ${p.name}, leaving...")
                                leaveGame()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getHypixelStats(uuid: String): JsonObject? {
        val key = Config.get("apiKey")
        if (key == "" || uuid == "") {
            return null
        }
        val url = URL("https://api.hypixel.net/player?uuid=$uuid&key=$key")
        val connection = url.openConnection()
        var res = ""
        BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
            var line: String?
            while (inp.readLine().also { line = it } != null) {
                res += line
            }
        }
        return Gson().fromJson(res, JsonObject::class.java)
    }

    private fun unameToUUID(uname: String): String {
        val url = URL("https://api.mojang.com/users/profiles/minecraft/$uname")
        val connection = url.openConnection()
        var res = ""
        BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
            var line: String?
            while (inp.readLine().also { line = it } != null) {
                res += line
            }
        }
        return res
    }

}