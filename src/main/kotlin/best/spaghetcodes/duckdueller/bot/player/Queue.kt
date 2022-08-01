package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object Queue {

    var inGame = false
    val playerCache = HashMap<String, String>()
    var ticksSinceStarted = 0
    var gameFull = false

    fun joinGame(msg: String) {
        if (!DuckDueller.getBot()?.gameStarted!!)
            DuckDueller.mc.thePlayer.sendChatMessage(msg)
    }

    fun leaveGame() {
        if (!DuckDueller.getBot()?.gameStarted!!) {
            DuckDueller.mc.thePlayer.sendChatMessage("/l")
            TimeUtils.setTimeout(fun () {
                LobbyMovement.stop() // this prevents moving in the lobby (looks a little sus)
            }, 2000) // wait just in case lel
        }
    }

    @SubscribeEvent
    fun onChatMessage(ev: ClientChatReceivedEvent) {
        val unformatted = ev.message.unformattedText
        if (unformatted.matches(Regex(".* has joined \\(./2\\)!"))) {
            inGame = true
            if (unformatted.matches(Regex(".* has joined \\(2/2\\)!"))) {
                gameFull = true
            }
        } else if (unformatted.lowercase().contains("something went wrong trying") || unformatted.lowercase().contains("please don't spam the command")) {
            TimeUtils.setTimeout(fun () {
                if (DuckDueller.getBot()?.queueCommand != "")
                    joinGame(DuckDueller.getBot()?.queueCommand!!)
            }, RandomUtils.randomIntInRange(6000, 8000))
        } else if (unformatted.contains("Are you sure? Type /lobby again")) {
            TimeUtils.setTimeout(fun () {
                DuckDueller.mc.thePlayer.sendChatMessage("/l")
            }, 50)
        } else if (unformatted.contains("A disconnect occurred in your connection, so you were put")) {
            TimeUtils.setTimeout(fun () {
                if (DuckDueller.getBot()?.queueCommand != "")
                    joinGame(DuckDueller.getBot()?.queueCommand!!)
            }, RandomUtils.randomIntInRange(4000, 6000))
        }
    }

    @SubscribeEvent
    fun onJoinWorld(ev: EntityJoinWorldEvent) {
        if (DuckDueller.mc.thePlayer != null && ev.entity == DuckDueller.mc.thePlayer) {
            LobbyMovement.stop()
            inGame = false
            gameFull = false
        }
    }

    @SubscribeEvent
    fun onDisconnect(ev: ClientDisconnectionFromServerEvent) {
        inGame = false
        gameFull = false
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (DuckDueller.getBot()?.isToggled() == true && DuckDueller.getBot()?.gameStarted == false && !gameFull) {
            // re-queue if no game starts after 60 seconds, doubles as failsafe for when the bot is stuck in a lobby
            ticksSinceStarted++

            val _1min = 20 * 60
            if (ticksSinceStarted > _1min) {
                ChatUtils.info("No game started in 60s, joining new game.")
                ticksSinceStarted = 0
                joinGame(DuckDueller.getBot()?.queueCommand!!)
            }
        } else {
            ticksSinceStarted = 0
        }
    }

    private fun logTeamPacket(packet: S3EPacketTeams) {
        val file = File(DuckDueller.mc.mcDataDir.absolutePath + "/logs/duckdueller_teampackets.txt")
        if (!file.exists()) {
            file.createNewFile()
        }

        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write("===============================================\n")
            writer.write("func_149312_c: ${packet.func_149312_c()}\n")
            writer.write("func_149306_d: ${packet.func_149306_d()}\n")
            writer.write("func_149311_e: ${packet.func_149311_e()}\n")
            writer.write("func_149309_f: ${packet.func_149309_f()}\n")
            writer.write("func_149310_g: ${packet.func_149310_g().joinToString(", ")}\n")
            writer.write("func_149307_h: ${packet.func_149307_h()}\n")
            writer.write("func_149308_i: ${packet.func_149308_i()}\n")
            writer.write("func_179813_h: ${packet.func_179813_h()}\n")
            writer.write("func_179814_i: ${packet.func_179814_i()}\n")
        }
    }

    fun teamPacket(packet: S3EPacketTeams) {
        if (packet.func_149307_h() == 3 && packet.func_149312_c() == "§7§k") { // mode 3 is for adding entities to a team
            val entities = packet.func_149310_g() // this is the entity list
            for (entity in entities) {
                if (entity.length > 2) { // hypixel sends fake entities with the names just being an emoji (??? why lol)
                    // small delay for the inGame state to update
                    TimeUtils.setTimeout(fun() { if (inGame) { checkPlayer(entity) } }, 1500)
                }
            }
        }
    }

    private fun checkPlayer(player: String) {
        // https://api.mojang.com/users/profiles/minecraft/<player>
        thread {
            var p: JSONDataClasses.Player? = null
            if (playerCache.containsKey(player)) {
                //ChatUtils.info("Player $player is already in the cache")
                p = JSONDataClasses.Player(player, playerCache[player]!!)
            } else {
                val res = unameToUUID(player)
                //ChatUtils.info("Checking $player")
                if (res != "") { // api returns 204 no content for invalid players
                    println("Deserializing $res")
                    val gson = Gson()
                    p = gson.fromJson(res, JSONDataClasses.Player::class.java)
                } else {
                    // nick, lets check dodgePlayers anyway
                    if (DuckDueller.getBot()?.playersLost?.contains(player) == true) {
                        ChatUtils.info("Lost to $player before, dodging...")
                        leaveGame()
                        TimeUtils.setTimeout(fun () { DuckDueller.getBot()?.queueCommand?.let { joinGame(it) } }, RandomUtils.randomIntInRange(4000, 8000))
                    }
                }
            }
            if (p != null) {
                playerCache[player] = p.id
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